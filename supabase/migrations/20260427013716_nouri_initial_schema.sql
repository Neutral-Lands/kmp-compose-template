-- =============================================================================
-- Nouri — Full Database Schema
-- Migration: initial schema covering all tables (E0–E8)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Extensions
-- ---------------------------------------------------------------------------
create extension if not exists "uuid-ossp";

-- ---------------------------------------------------------------------------
-- Enums
-- ---------------------------------------------------------------------------
create type user_role as enum ('nutritionist', 'patient');

create type assignment_status as enum ('pending', 'accepted', 'rejected', 'inactive');

create type compliance_status as enum (
  'followed',
  'not_followed_healthy',
  'not_followed_not_healthy',
  'skipped'
);

create type appointment_status as enum (
  'scheduled',
  'reschedule_requested',
  'rescheduled',
  'cancelled',
  'completed'
);

create type reschedule_requester as enum ('patient', 'nutritionist');

create type water_status as enum ('met', 'not_met');

-- ---------------------------------------------------------------------------
-- profiles
-- Extends auth.users. One row per user, created on sign-up via trigger.
-- ---------------------------------------------------------------------------
create table profiles (
  id              uuid primary key references auth.users(id) on delete cascade,
  role            user_role        not null,
  full_name       text             not null,
  avatar_url      text,
  birth_date      date,
  fcm_token       text,                        -- Android push token
  apns_token      text,                        -- iOS push token
  -- Notification preferences
  notif_appointment_reminders  boolean not null default true,
  notif_new_meal_plan          boolean not null default true,
  notif_assignment_requests    boolean not null default true,
  created_at      timestamptz      not null default now(),
  updated_at      timestamptz      not null default now()
);

-- ---------------------------------------------------------------------------
-- subscription_plans
-- Static lookup table — seeded, not user-editable.
-- ---------------------------------------------------------------------------
create table subscription_plans (
  id              text             primary key,  -- 'free' | 'starter' | 'pro'
  name            text             not null,
  max_patients    integer          not null,     -- -1 = unlimited
  price_usd_cents integer          not null      -- 0 = free
);

-- ---------------------------------------------------------------------------
-- nutritionist_subscriptions
-- One active subscription per nutritionist.
-- ---------------------------------------------------------------------------
create table nutritionist_subscriptions (
  id                    uuid primary key default uuid_generate_v4(),
  nutritionist_id       uuid not null references profiles(id) on delete cascade,
  plan_id               text not null references subscription_plans(id),
  -- cumulative_patients tracks total ever-assigned (slot consumption, not current count)
  cumulative_patients   integer not null default 0,
  created_at            timestamptz not null default now(),
  updated_at            timestamptz not null default now(),
  unique (nutritionist_id)
);

-- ---------------------------------------------------------------------------
-- nutritionist_patients
-- Assignment relationship between nutritionist and patient.
-- A patient-nutritionist assignment permanently consumes a slot.
-- ---------------------------------------------------------------------------
create table nutritionist_patients (
  id                uuid primary key default uuid_generate_v4(),
  nutritionist_id   uuid not null references profiles(id) on delete cascade,
  patient_id        uuid not null references profiles(id) on delete cascade,
  status            assignment_status not null default 'pending',
  requested_at      timestamptz not null default now(),
  responded_at      timestamptz,
  unique (nutritionist_id, patient_id)
);

-- ---------------------------------------------------------------------------
-- meal_plans
-- One active plan per patient at a time. PDF stored in Supabase Storage.
-- Naming convention: {nutritionist_id}/{patient_id}/{timestamp}.pdf
-- ---------------------------------------------------------------------------
create table meal_plans (
  id                uuid primary key default uuid_generate_v4(),
  nutritionist_id   uuid not null references profiles(id) on delete cascade,
  patient_id        uuid not null references profiles(id) on delete cascade,
  storage_path      text not null,   -- path inside meal-plans bucket
  file_name         text not null,
  is_active         boolean not null default true,
  version           integer not null default 1,
  uploaded_at       timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- meal_plan_slots
-- Nutritionist defines named slots per plan (e.g. "Pre-workout", "Lunch").
-- ---------------------------------------------------------------------------
create table meal_plan_slots (
  id            uuid primary key default uuid_generate_v4(),
  meal_plan_id  uuid not null references meal_plans(id) on delete cascade,
  index         integer not null,   -- display order, 0-based
  name          text not null,
  created_at    timestamptz not null default now(),
  unique (meal_plan_id, index)
);

-- ---------------------------------------------------------------------------
-- compliance_logs
-- Daily meal compliance per patient. Slots defined by nutritionist per plan.
-- Patient can log/edit any past day. Cannot log future dates.
-- ---------------------------------------------------------------------------
create table compliance_logs (
  id                  uuid primary key default uuid_generate_v4(),
  patient_id          uuid not null references profiles(id) on delete cascade,
  meal_plan_slot_id   uuid not null references meal_plan_slots(id) on delete cascade,
  log_date            date not null,
  status              compliance_status not null,
  note                text,
  synced              boolean not null default false,
  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now(),
  unique (patient_id, meal_plan_slot_id, log_date)
);

-- ---------------------------------------------------------------------------
-- water_logs
-- Binary per day — met or not met.
-- ---------------------------------------------------------------------------
create table water_logs (
  id            uuid primary key default uuid_generate_v4(),
  patient_id    uuid not null references profiles(id) on delete cascade,
  log_date      date not null,
  status        water_status not null,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  unique (patient_id, log_date)
);

-- ---------------------------------------------------------------------------
-- exercise_logs
-- Binary per day — met or not met.
-- ---------------------------------------------------------------------------
create table exercise_logs (
  id            uuid primary key default uuid_generate_v4(),
  patient_id    uuid not null references profiles(id) on delete cascade,
  log_date      date not null,
  status        water_status not null,  -- reuses met/not_met enum
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  unique (patient_id, log_date)
);

-- ---------------------------------------------------------------------------
-- appointments
-- Full lifecycle between nutritionist and patient.
-- ---------------------------------------------------------------------------
create table appointments (
  id                        uuid primary key default uuid_generate_v4(),
  nutritionist_id           uuid not null references profiles(id) on delete cascade,
  patient_id                uuid not null references profiles(id) on delete cascade,
  scheduled_at              timestamptz not null,
  duration_minutes          integer not null default 60,
  status                    appointment_status not null default 'scheduled',
  reschedule_requested_by   reschedule_requester,
  proposed_reschedule_at    timestamptz,
  notes                     text,
  created_at                timestamptz not null default now(),
  updated_at                timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- anthropometric_measurements
-- Logged by nutritionist per consultation. Patient is read-only.
-- All measurements stored in metric (kg, cm).
-- ---------------------------------------------------------------------------
create table anthropometric_measurements (
  id                uuid primary key default uuid_generate_v4(),
  patient_id        uuid not null references profiles(id) on delete cascade,
  nutritionist_id   uuid not null references profiles(id) on delete cascade,
  measured_at       timestamptz not null,

  -- Basic
  weight_kg         numeric(5,2),
  height_cm         numeric(5,2),

  -- Perimeters (cm)
  arm_relaxed_cm    numeric(5,2),
  arm_flexed_cm     numeric(5,2),
  chest_cm          numeric(5,2),
  waist_cm          numeric(5,2),
  hip_cm            numeric(5,2),
  mid_thigh_cm      numeric(5,2),
  calf_cm           numeric(5,2),

  -- Skin folds (mm)
  sf_triceps_mm         numeric(4,1),
  sf_subscapular_mm     numeric(4,1),
  sf_biceps_mm          numeric(4,1),
  sf_suprailiac_mm      numeric(4,1),
  sf_supraspinal_mm     numeric(4,1),
  sf_abdominal_mm       numeric(4,1),
  sf_mid_thigh_mm       numeric(4,1),
  sf_calf_mm            numeric(4,1),

  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now()
);

-- =============================================================================
-- INDEXES
-- =============================================================================
create index on nutritionist_patients (nutritionist_id);
create index on nutritionist_patients (patient_id);
create index on meal_plans (patient_id, is_active);
create index on meal_plan_slots (meal_plan_id);
create index on compliance_logs (patient_id, log_date);
create index on compliance_logs (meal_plan_slot_id);
create index on exercise_logs (patient_id, log_date);
create index on water_logs (patient_id, log_date);
create index on appointments (nutritionist_id, scheduled_at);
create index on appointments (patient_id, scheduled_at);
create index on anthropometric_measurements (patient_id, measured_at);

-- =============================================================================
-- TRIGGERS — updated_at auto-maintenance
-- =============================================================================
create or replace function set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger trg_profiles_updated_at
  before update on profiles
  for each row execute function set_updated_at();

create trigger trg_nutritionist_subscriptions_updated_at
  before update on nutritionist_subscriptions
  for each row execute function set_updated_at();

create trigger trg_compliance_logs_updated_at
  before update on compliance_logs
  for each row execute function set_updated_at();

create trigger trg_water_logs_updated_at
  before update on water_logs
  for each row execute function set_updated_at();

create trigger trg_exercise_logs_updated_at
  before update on exercise_logs
  for each row execute function set_updated_at();

create trigger trg_appointments_updated_at
  before update on appointments
  for each row execute function set_updated_at();

create trigger trg_anthropometric_measurements_updated_at
  before update on anthropometric_measurements
  for each row execute function set_updated_at();

-- =============================================================================
-- TRIGGER — auto-create profile on auth.users insert
-- =============================================================================
create or replace function handle_new_user()
returns trigger language plpgsql security definer as $$
begin
  insert into profiles (id, role, full_name)
  values (
    new.id,
    (new.raw_user_meta_data->>'role')::user_role,
    coalesce(new.raw_user_meta_data->>'full_name', '')
  );
  return new;
end;
$$;

create trigger trg_on_auth_user_created
  after insert on auth.users
  for each row execute function handle_new_user();

-- =============================================================================
-- TRIGGER — enforce patient slot limit (NEU-69)
-- Slot based on cumulative_patients, not current active count.
-- =============================================================================
create or replace function enforce_patient_slot_limit()
returns trigger language plpgsql as $$
declare
  v_plan_id       text;
  v_max_patients  integer;
  v_cumulative    integer;
begin
  -- Only enforce on new accepted assignments
  if new.status != 'accepted' then
    return new;
  end if;
  if old.status = 'accepted' then
    return new; -- already counted
  end if;

  select ns.plan_id, sp.max_patients, ns.cumulative_patients
  into v_plan_id, v_max_patients, v_cumulative
  from nutritionist_subscriptions ns
  join subscription_plans sp on sp.id = ns.plan_id
  where ns.nutritionist_id = new.nutritionist_id;

  if v_max_patients != -1 and v_cumulative >= v_max_patients then
    raise exception 'Patient slot limit reached for plan %', v_plan_id;
  end if;

  -- Increment cumulative counter
  update nutritionist_subscriptions
  set cumulative_patients = cumulative_patients + 1
  where nutritionist_id = new.nutritionist_id;

  return new;
end;
$$;

create trigger trg_enforce_patient_slot_limit
  before update on nutritionist_patients
  for each row execute function enforce_patient_slot_limit();

-- =============================================================================
-- ROW LEVEL SECURITY
-- =============================================================================

alter table profiles                      enable row level security;
alter table subscription_plans            enable row level security;
alter table nutritionist_subscriptions    enable row level security;
alter table nutritionist_patients         enable row level security;
alter table meal_plans                    enable row level security;
alter table compliance_logs               enable row level security;
alter table water_logs                    enable row level security;
alter table exercise_logs                 enable row level security;
alter table appointments                  enable row level security;
alter table anthropometric_measurements   enable row level security;

-- ---------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------
create policy "profiles: own read"
  on profiles for select
  using (auth.uid() = id);

create policy "profiles: own update"
  on profiles for update
  using (auth.uid() = id);

-- Nutritionist can read profiles of their assigned patients
create policy "profiles: nutritionist reads patients"
  on profiles for select
  using (
    exists (
      select 1 from nutritionist_patients np
      where np.nutritionist_id = auth.uid()
        and np.patient_id = profiles.id
        and np.status = 'accepted'
    )
  );

-- Patient can read profile of their nutritionist
create policy "profiles: patient reads nutritionist"
  on profiles for select
  using (
    exists (
      select 1 from nutritionist_patients np
      where np.patient_id = auth.uid()
        and np.nutritionist_id = profiles.id
        and np.status = 'accepted'
    )
  );

-- ---------------------------------------------------------------------------
-- subscription_plans (public read — shown during onboarding)
-- ---------------------------------------------------------------------------
create policy "subscription_plans: public read"
  on subscription_plans for select
  using (true);

-- ---------------------------------------------------------------------------
-- nutritionist_subscriptions
-- ---------------------------------------------------------------------------
create policy "nutritionist_subscriptions: own read/write"
  on nutritionist_subscriptions for all
  using (auth.uid() = nutritionist_id);

-- ---------------------------------------------------------------------------
-- nutritionist_patients
-- ---------------------------------------------------------------------------
create policy "nutritionist_patients: nutritionist all"
  on nutritionist_patients for all
  using (auth.uid() = nutritionist_id);

create policy "nutritionist_patients: patient read/respond"
  on nutritionist_patients for select
  using (auth.uid() = patient_id);

create policy "nutritionist_patients: patient update status"
  on nutritionist_patients for update
  using (auth.uid() = patient_id);

-- ---------------------------------------------------------------------------
-- meal_plans
-- ---------------------------------------------------------------------------
create policy "meal_plans: nutritionist all"
  on meal_plans for all
  using (auth.uid() = nutritionist_id);

create policy "meal_plans: patient read own"
  on meal_plans for select
  using (auth.uid() = patient_id);

-- ---------------------------------------------------------------------------
-- compliance_logs
-- ---------------------------------------------------------------------------
create policy "compliance_logs: patient all"
  on compliance_logs for all
  using (auth.uid() = patient_id);

create policy "compliance_logs: nutritionist read assigned"
  on compliance_logs for select
  using (
    exists (
      select 1 from nutritionist_patients np
      where np.nutritionist_id = auth.uid()
        and np.patient_id = compliance_logs.patient_id
        and np.status = 'accepted'
    )
  );

-- ---------------------------------------------------------------------------
-- water_logs
-- ---------------------------------------------------------------------------
create policy "water_logs: patient all"
  on water_logs for all
  using (auth.uid() = patient_id);

create policy "water_logs: nutritionist read assigned"
  on water_logs for select
  using (
    exists (
      select 1 from nutritionist_patients np
      where np.nutritionist_id = auth.uid()
        and np.patient_id = water_logs.patient_id
        and np.status = 'accepted'
    )
  );

-- ---------------------------------------------------------------------------
-- exercise_logs
-- ---------------------------------------------------------------------------
create policy "exercise_logs: patient all"
  on exercise_logs for all
  using (auth.uid() = patient_id);

create policy "exercise_logs: nutritionist read assigned"
  on exercise_logs for select
  using (
    exists (
      select 1 from nutritionist_patients np
      where np.nutritionist_id = auth.uid()
        and np.patient_id = exercise_logs.patient_id
        and np.status = 'accepted'
    )
  );

-- ---------------------------------------------------------------------------
-- appointments
-- ---------------------------------------------------------------------------
create policy "appointments: nutritionist all"
  on appointments for all
  using (auth.uid() = nutritionist_id);

create policy "appointments: patient read/update"
  on appointments for select
  using (auth.uid() = patient_id);

create policy "appointments: patient request reschedule"
  on appointments for update
  using (auth.uid() = patient_id);

-- ---------------------------------------------------------------------------
-- anthropometric_measurements
-- ---------------------------------------------------------------------------
create policy "anthropometric_measurements: nutritionist all"
  on anthropometric_measurements for all
  using (auth.uid() = nutritionist_id);

create policy "anthropometric_measurements: patient read own"
  on anthropometric_measurements for select
  using (auth.uid() = patient_id);

-- =============================================================================
-- STORAGE RLS — meal-plans bucket
-- Applied via Supabase Storage policies (SQL mirror shown here for reference)
-- =============================================================================
-- Nutritionist can upload to their own folder: {nutritionist_id}/{patient_id}/...
-- Patient can only download from their own folder: .../{patient_id}/...
-- No cross-patient access.
