-- LMS System Database Schema (PostgreSQL)
-- Note: No seed data included. Use Flyway for managed migrations.
-- This script is safe to run manually against an empty Postgres database.
-- Assumptions:
-- - Database already exists (Neon DB). Drop/Create database and USE are removed.
-- - ID columns use GENERATED ALWAYS AS IDENTITY
-- - updated_at managed via trigger on tables that need it
-- - JSON columns use jsonb
-- - ENUMs replaced with CHECK constraints or domain-like text with CHECK
-- - Full-text indexes replaced with GIN indexes on tsvector expressions
--
-- Helpers: updated_at trigger
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Helper: set last_updated column (for tables that use this name instead of updated_at)
CREATE OR REPLACE FUNCTION set_last_updated()
RETURNS TRIGGER AS $$
BEGIN
  NEW.last_updated = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- CORE USER MANAGEMENT TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  username VARCHAR(50) UNIQUE,
  is_active BOOLEAN DEFAULT TRUE,
  is_verified BOOLEAN DEFAULT FALSE,
  email_verified_at TIMESTAMPTZ,
  last_login TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_created_at ON users(created_at);

CREATE TABLE IF NOT EXISTS user_profiles (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  bio TEXT,
  avatar_url VARCHAR(500),
  phone VARCHAR(20),
  date_of_birth DATE,
  timezone VARCHAR(50) DEFAULT 'UTC',
  language VARCHAR(10) DEFAULT 'en',
  preferences JSONB,
  social_links JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_sessions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  session_token VARCHAR(255) NOT NULL UNIQUE,
  refresh_token VARCHAR(255) UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  ip_address VARCHAR(45),
  user_agent TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_session_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_expires_at ON user_sessions(expires_at);

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  display_name VARCHAR(100) NOT NULL,
  description TEXT,
  is_system_role BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS permissions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  display_name VARCHAR(150) NOT NULL,
  description TEXT,
  resource VARCHAR(50),
  action VARCHAR(50),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  assigned_at TIMESTAMPTZ DEFAULT NOW(),
  assigned_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  PRIMARY KEY (user_id, role_id)
);

-- =============================================
-- COURSE STRUCTURE TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
  sort_order INT DEFAULT 0,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
CREATE INDEX IF NOT EXISTS idx_parent_id ON categories(parent_id);

CREATE TABLE IF NOT EXISTS courses (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  short_description TEXT,
  thumbnail_url VARCHAR(500),
  category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
  instructor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  difficulty_level TEXT DEFAULT 'beginner' CHECK (difficulty_level IN ('beginner','intermediate','advanced')),
  duration_hours INT DEFAULT 0,
  max_enrollments INT,
  enrollment_start DATE,
  enrollment_end DATE,
  course_start DATE,
  course_end DATE,
  price NUMERIC(10,2) DEFAULT 0.00,
  is_free BOOLEAN DEFAULT TRUE,
  is_published BOOLEAN DEFAULT FALSE,
  is_featured BOOLEAN DEFAULT FALSE,
  enrollment_type TEXT DEFAULT 'open' CHECK (enrollment_type IN ('open','approval_required','invitation_only')),
  certificate_enabled BOOLEAN DEFAULT FALSE,
  prerequisites JSONB,
  learning_objectives JSONB,
  tags JSONB,
  metadata JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_courses_slug ON courses(slug);
CREATE INDEX IF NOT EXISTS idx_instructor_id ON courses(instructor_id);
CREATE INDEX IF NOT EXISTS idx_category_id ON courses(category_id);
CREATE INDEX IF NOT EXISTS idx_is_published ON courses(is_published);
CREATE INDEX IF NOT EXISTS idx_courses_created_at ON courses(created_at);

CREATE TABLE IF NOT EXISTS course_modules (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  sort_order INT DEFAULT 0,
  is_published BOOLEAN DEFAULT FALSE,
  unlock_conditions JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_course_id ON course_modules(course_id);
CREATE INDEX IF NOT EXISTS idx_module_sort_order ON course_modules(sort_order);

CREATE TABLE IF NOT EXISTS lessons (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  module_id BIGINT NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  content_type TEXT NOT NULL CHECK (content_type IN ('text','video','audio','interactive','game','assessment')),
  content_data JSONB,
  duration_minutes INT DEFAULT 0,
  sort_order INT DEFAULT 0,
  is_published BOOLEAN DEFAULT FALSE,
  is_required BOOLEAN DEFAULT TRUE,
  unlock_conditions JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_lessons_module_id ON lessons(module_id);
CREATE INDEX IF NOT EXISTS idx_lessons_sort_order ON lessons(sort_order);
CREATE INDEX IF NOT EXISTS idx_lessons_content_type ON lessons(content_type);

CREATE TABLE IF NOT EXISTS course_enrollments (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  enrollment_status TEXT DEFAULT 'active' CHECK (enrollment_status IN ('active','completed','dropped','suspended')),
  enrollment_date TIMESTAMPTZ DEFAULT NOW(),
  completion_date TIMESTAMPTZ,
  progress_percentage NUMERIC(5,2) DEFAULT 0.00,
  last_accessed TIMESTAMPTZ,
  certificate_issued_at TIMESTAMPTZ,
  certificate_url VARCHAR(500),
  notes TEXT,
  CONSTRAINT unique_enrollment UNIQUE (user_id, course_id)
);
CREATE INDEX IF NOT EXISTS idx_enroll_user_id ON course_enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enroll_course_id ON course_enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enroll_status ON course_enrollments(enrollment_status);

CREATE TABLE IF NOT EXISTS course_instructors (
  course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role TEXT DEFAULT 'co-instructor' CHECK (role IN ('primary','co-instructor','assistant')),
  assigned_at TIMESTAMPTZ DEFAULT NOW(),
  PRIMARY KEY (course_id, user_id)
);

-- =============================================
-- GAME INTEGRATION TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS games (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  game_version VARCHAR(20) NOT NULL,
  game_file_path VARCHAR(500) NOT NULL,
  thumbnail_url VARCHAR(500),
  game_type TEXT DEFAULT 'web' CHECK (game_type IN ('web','download','embedded')),
  engine_version VARCHAR(50),
  difficulty_level TEXT DEFAULT 'medium' CHECK (difficulty_level IN ('easy','medium','hard')),
  estimated_play_time INT DEFAULT 0,
  learning_objectives JSONB,
  game_config JSONB,
  is_active BOOLEAN DEFAULT TRUE,
  is_multiplayer BOOLEAN DEFAULT FALSE,
  max_players INT DEFAULT 1,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_games_slug ON games(slug);
CREATE INDEX IF NOT EXISTS idx_games_created_by ON games(created_by);
CREATE INDEX IF NOT EXISTS idx_games_is_active ON games(is_active);

CREATE TABLE IF NOT EXISTS game_levels (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  level_number INT NOT NULL,
  level_name VARCHAR(255) NOT NULL,
  description TEXT,
  unlock_conditions JSONB,
  completion_criteria JSONB,
  max_score INT DEFAULT 0,
  time_limit INT,
  difficulty_rating INT DEFAULT 1,
  learning_objectives JSONB,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT unique_game_level UNIQUE (game_id, level_number)
);
CREATE INDEX IF NOT EXISTS idx_game_levels_game_id ON game_levels(game_id);
CREATE INDEX IF NOT EXISTS idx_level_number ON game_levels(level_number);

CREATE TABLE IF NOT EXISTS game_sessions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  session_token VARCHAR(255) NOT NULL UNIQUE,
  started_at TIMESTAMPTZ DEFAULT NOW(),
  ended_at TIMESTAMPTZ,
  duration_seconds INT DEFAULT 0,
  final_score INT DEFAULT 0,
  levels_completed INT DEFAULT 0,
  session_data JSONB,
  device_info JSONB,
  ip_address VARCHAR(45)
);
CREATE INDEX IF NOT EXISTS idx_game_sessions_user_id ON game_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_game_sessions_game_id ON game_sessions(game_id);
CREATE INDEX IF NOT EXISTS idx_session_token ON game_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_game_sessions_started_at ON game_sessions(started_at);

CREATE TABLE IF NOT EXISTS game_progress (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  current_level INT DEFAULT 1,
  highest_level_reached INT DEFAULT 1,
  total_score INT DEFAULT 0,
  best_score INT DEFAULT 0,
  total_play_time INT DEFAULT 0,
  completion_percentage NUMERIC(5,2) DEFAULT 0.00,
  last_played TIMESTAMPTZ,
  progress_data JSONB,
  achievements_unlocked JSONB,
  CONSTRAINT unique_user_game UNIQUE (user_id, game_id)
);
CREATE INDEX IF NOT EXISTS idx_game_progress_user_id ON game_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_game_progress_game_id ON game_progress(game_id);

CREATE TABLE IF NOT EXISTS game_level_progress (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_level_id BIGINT NOT NULL REFERENCES game_levels(id) ON DELETE CASCADE,
  is_completed BOOLEAN DEFAULT FALSE,
  best_score INT DEFAULT 0,
  completion_time INT,
  attempts INT DEFAULT 0,
  first_completed_at TIMESTAMPTZ,
  last_attempted_at TIMESTAMPTZ,
  progress_data JSONB,
  CONSTRAINT unique_user_level UNIQUE (user_id, game_level_id)
);
CREATE INDEX IF NOT EXISTS idx_glp_user_id ON game_level_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_glp_game_level_id ON game_level_progress(game_level_id);

CREATE TABLE IF NOT EXISTS game_analytics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  session_id BIGINT NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
  event_type VARCHAR(100) NOT NULL,
  event_data JSONB NOT NULL,
  timestamp TIMESTAMPTZ DEFAULT NOW(),
  level_id BIGINT REFERENCES game_levels(id) ON DELETE SET NULL,
  score_delta INT DEFAULT 0,
  custom_properties JSONB
);
CREATE INDEX IF NOT EXISTS idx_ga_session_id ON game_analytics(session_id);
CREATE INDEX IF NOT EXISTS idx_ga_event_type ON game_analytics(event_type);
CREATE INDEX IF NOT EXISTS idx_ga_timestamp ON game_analytics(timestamp);

CREATE TABLE IF NOT EXISTS game_saves (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  save_slot INT DEFAULT 1,
  save_name VARCHAR(255),
  save_data JSONB NOT NULL,
  screenshot_url VARCHAR(500),
  game_version VARCHAR(20),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT unique_user_game_slot UNIQUE (user_id, game_id, save_slot)
);
CREATE INDEX IF NOT EXISTS idx_game_saves_user_id ON game_saves(user_id);
CREATE INDEX IF NOT EXISTS idx_game_saves_game_id ON game_saves(game_id);

-- =============================================
-- ASSESSMENT SYSTEM TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS assessments (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
  lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
  assessment_type TEXT DEFAULT 'quiz' CHECK (assessment_type IN ('quiz','exam','assignment','survey')),
  time_limit INT,
  max_attempts INT DEFAULT 1,
  passing_score NUMERIC(5,2) DEFAULT 70.00,
  is_randomized BOOLEAN DEFAULT FALSE,
  show_correct_answers BOOLEAN DEFAULT FALSE,
  allow_review BOOLEAN DEFAULT TRUE,
  is_published BOOLEAN DEFAULT FALSE,
  available_from TIMESTAMPTZ,
  available_until TIMESTAMPTZ,
  instructions TEXT,
  settings JSONB,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_assess_course_id ON assessments(course_id);
CREATE INDEX IF NOT EXISTS idx_assess_lesson_id ON assessments(lesson_id);
CREATE INDEX IF NOT EXISTS idx_assess_created_by ON assessments(created_by);

CREATE TABLE IF NOT EXISTS assessment_questions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
  question_type TEXT NOT NULL CHECK (question_type IN ('multiple_choice','true_false','short_answer','essay','fill_blank','matching','ordering')),
  question_text TEXT NOT NULL,
  points NUMERIC(5,2) DEFAULT 1.00,
  sort_order INT DEFAULT 0,
  is_required BOOLEAN DEFAULT TRUE,
  question_data JSONB,
  explanation TEXT,
  tags JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_aq_assessment_id ON assessment_questions(assessment_id);
CREATE INDEX IF NOT EXISTS idx_aq_question_type ON assessment_questions(question_type);

CREATE TABLE IF NOT EXISTS question_options (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
  option_text TEXT NOT NULL,
  is_correct BOOLEAN DEFAULT FALSE,
  sort_order INT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_qo_question_id ON question_options(question_id);

CREATE TABLE IF NOT EXISTS student_assessments (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
  attempt_number INT DEFAULT 1,
  started_at TIMESTAMPTZ DEFAULT NOW(),
  submitted_at TIMESTAMPTZ,
  time_taken INT,
  score NUMERIC(5,2),
  max_score NUMERIC(5,2),
  percentage NUMERIC(5,2),
  passed BOOLEAN DEFAULT FALSE,
  status TEXT DEFAULT 'in_progress' CHECK (status IN ('in_progress','submitted','graded','expired')),
  graded_at TIMESTAMPTZ,
  graded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  feedback TEXT,
  attempt_data JSONB
);
CREATE INDEX IF NOT EXISTS idx_sa_user_id ON student_assessments(user_id);
CREATE INDEX IF NOT EXISTS idx_sa_assessment_id ON student_assessments(assessment_id);
CREATE INDEX IF NOT EXISTS idx_sa_status ON student_assessments(status);

CREATE TABLE IF NOT EXISTS student_answers (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  attempt_id BIGINT NOT NULL REFERENCES student_assessments(id) ON DELETE CASCADE,
  question_id BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
  answer_data JSONB NOT NULL,
  points_earned NUMERIC(5,2) DEFAULT 0.00,
  is_correct BOOLEAN,
  answered_at TIMESTAMPTZ DEFAULT NOW(),
  feedback TEXT,
  CONSTRAINT unique_attempt_question UNIQUE (attempt_id, question_id)
);
CREATE INDEX IF NOT EXISTS idx_sans_attempt_id ON student_answers(attempt_id);
CREATE INDEX IF NOT EXISTS idx_sans_question_id ON student_answers(question_id);

-- =============================================
-- PROGRESS TRACKING TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS learning_paths (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  thumbnail_url VARCHAR(500),
  estimated_duration INT DEFAULT 0,
  difficulty_level TEXT DEFAULT 'beginner' CHECK (difficulty_level IN ('beginner','intermediate','advanced')),
  is_published BOOLEAN DEFAULT FALSE,
  sort_order INT DEFAULT 0,
  prerequisites JSONB,
  learning_objectives JSONB,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_lp_created_by ON learning_paths(created_by);
CREATE INDEX IF NOT EXISTS idx_lp_is_published ON learning_paths(is_published);

CREATE TABLE IF NOT EXISTS learning_path_items (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  learning_path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
  item_type TEXT NOT NULL CHECK (item_type IN ('course','game','assessment')),
  item_id BIGINT NOT NULL,
  sort_order INT DEFAULT 0,
  is_required BOOLEAN DEFAULT TRUE,
  unlock_conditions JSONB,
  completion_criteria JSONB
);
CREATE INDEX IF NOT EXISTS idx_lpi_learning_path_id ON learning_path_items(learning_path_id);
CREATE INDEX IF NOT EXISTS idx_lpi_item_type_id ON learning_path_items(item_type, item_id);

CREATE TABLE IF NOT EXISTS learning_path_progress (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  learning_path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
  enrolled_at TIMESTAMPTZ DEFAULT NOW(),
  current_item_id BIGINT REFERENCES learning_path_items(id) ON DELETE SET NULL,
  completion_percentage NUMERIC(5,2) DEFAULT 0.00,
  completed_at TIMESTAMPTZ,
  last_accessed TIMESTAMPTZ,
  CONSTRAINT unique_user_path UNIQUE (user_id, learning_path_id)
);
CREATE INDEX IF NOT EXISTS idx_lpp_user_id ON learning_path_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_lpp_learning_path_id ON learning_path_progress(learning_path_id);

CREATE TABLE IF NOT EXISTS student_progress (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  item_type TEXT NOT NULL CHECK (item_type IN ('course','module','lesson','game','assessment')),
  item_id BIGINT NOT NULL,
  progress_percentage NUMERIC(5,2) DEFAULT 0.00,
  status TEXT DEFAULT 'not_started' CHECK (status IN ('not_started','in_progress','completed','failed')),
  started_at TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  time_spent INT DEFAULT 0,
  last_accessed TIMESTAMPTZ,
  progress_data JSONB,
  CONSTRAINT unique_user_item UNIQUE (user_id, item_type, item_id)
);
CREATE INDEX IF NOT EXISTS idx_sp_user_id ON student_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_sp_item_type_id ON student_progress(item_type, item_id);
CREATE INDEX IF NOT EXISTS idx_sp_status ON student_progress(status);

CREATE TABLE IF NOT EXISTS learning_objectives (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  objective_type TEXT DEFAULT 'knowledge' CHECK (objective_type IN ('knowledge','skill','competency')),
  category VARCHAR(100),
  level TEXT DEFAULT 'basic' CHECK (level IN ('basic','intermediate','advanced')),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS objective_mappings (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  objective_id BIGINT NOT NULL REFERENCES learning_objectives(id) ON DELETE CASCADE,
  content_type TEXT NOT NULL CHECK (content_type IN ('course','lesson','game','assessment')),
  content_id BIGINT NOT NULL,
  weight NUMERIC(3,2) DEFAULT 1.00
);
CREATE INDEX IF NOT EXISTS idx_om_objective_id ON objective_mappings(objective_id);
CREATE INDEX IF NOT EXISTS idx_om_content_type_id ON objective_mappings(content_type, content_id);

CREATE TABLE IF NOT EXISTS mastery_tracking (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  objective_id BIGINT NOT NULL REFERENCES learning_objectives(id) ON DELETE CASCADE,
  mastery_level NUMERIC(5,2) DEFAULT 0.00,
  assessment_count INT DEFAULT 0,
  last_assessed TIMESTAMPTZ,
  mastery_achieved BOOLEAN DEFAULT FALSE,
  mastery_achieved_at TIMESTAMPTZ,
  CONSTRAINT unique_user_objective UNIQUE (user_id, objective_id)
);
CREATE INDEX IF NOT EXISTS idx_mt_user_id ON mastery_tracking(user_id);
CREATE INDEX IF NOT EXISTS idx_mt_objective_id ON mastery_tracking(objective_id);

-- =============================================
-- COMMUNICATION TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS forums (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
  forum_type TEXT DEFAULT 'general' CHECK (forum_type IN ('general','course','game','help')),
  is_active BOOLEAN DEFAULT TRUE,
  sort_order INT DEFAULT 0,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_forums_course_id ON forums(course_id);
CREATE INDEX IF NOT EXISTS idx_forums_forum_type ON forums(forum_type);

CREATE TABLE IF NOT EXISTS forum_topics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  forum_id BIGINT NOT NULL REFERENCES forums(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  is_pinned BOOLEAN DEFAULT FALSE,
  is_locked BOOLEAN DEFAULT FALSE,
  view_count INT DEFAULT 0,
  reply_count INT DEFAULT 0,
  last_post_id BIGINT,
  last_post_at TIMESTAMPTZ,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ft_forum_id ON forum_topics(forum_id);
CREATE INDEX IF NOT EXISTS idx_ft_created_by ON forum_topics(created_by);
CREATE INDEX IF NOT EXISTS idx_ft_last_post_at ON forum_topics(last_post_at);

CREATE TABLE IF NOT EXISTS forum_posts (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  topic_id BIGINT NOT NULL REFERENCES forum_topics(id) ON DELETE CASCADE,
  parent_post_id BIGINT REFERENCES forum_posts(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  is_edited BOOLEAN DEFAULT FALSE,
  edited_at TIMESTAMPTZ,
  edited_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  like_count INT DEFAULT 0,
  is_solution BOOLEAN DEFAULT FALSE,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_fp_topic_id ON forum_posts(topic_id);
CREATE INDEX IF NOT EXISTS idx_fp_parent_post_id ON forum_posts(parent_post_id);
CREATE INDEX IF NOT EXISTS idx_fp_created_by ON forum_posts(created_by);
CREATE INDEX IF NOT EXISTS idx_fp_created_at ON forum_posts(created_at);

CREATE TABLE IF NOT EXISTS messages (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  conversation_id VARCHAR(255) NOT NULL,
  sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  subject VARCHAR(255),
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  read_at TIMESTAMPTZ,
  parent_message_id BIGINT REFERENCES messages(id) ON DELETE SET NULL,
  attachment_url VARCHAR(500),
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_msg_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_msg_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_msg_recipient_id ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_msg_created_at ON messages(created_at);

CREATE TABLE IF NOT EXISTS announcements (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  announcement_type TEXT DEFAULT 'system' CHECK (announcement_type IN ('system','course','maintenance')),
  target_audience TEXT DEFAULT 'all' CHECK (target_audience IN ('all','students','instructors','admins')),
  course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
  priority TEXT DEFAULT 'normal' CHECK (priority IN ('low','normal','high','urgent')),
  is_published BOOLEAN DEFAULT FALSE,
  publish_at TIMESTAMPTZ,
  expire_at TIMESTAMPTZ,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ann_course_id ON announcements(course_id);
CREATE INDEX IF NOT EXISTS idx_ann_target_audience ON announcements(target_audience);
CREATE INDEX IF NOT EXISTS idx_ann_is_published ON announcements(is_published);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  notification_type VARCHAR(50) NOT NULL,
  related_type TEXT CHECK (related_type IN ('course','game','assessment','message','achievement','system')),
  related_id BIGINT,
  is_read BOOLEAN DEFAULT FALSE,
  read_at TIMESTAMPTZ,
  action_url VARCHAR(500),
  metadata JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notif_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notif_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notif_type ON notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_notif_created_at ON notifications(created_at);

-- =============================================
-- GAMIFICATION TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS achievements (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  achievement_type TEXT DEFAULT 'learning' CHECK (achievement_type IN ('course','game','learning','social','system')),
  badge_icon_url VARCHAR(500),
  points_reward INT DEFAULT 0,
  rarity TEXT DEFAULT 'common' CHECK (rarity IN ('common','uncommon','rare','epic','legendary')),
  unlock_criteria JSONB NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  is_hidden BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ach_type ON achievements(achievement_type);
CREATE INDEX IF NOT EXISTS idx_ach_is_active ON achievements(is_active);

CREATE TABLE IF NOT EXISTS student_achievements (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
  earned_at TIMESTAMPTZ DEFAULT NOW(),
  progress_data JSONB,
  awarded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT unique_user_achievement UNIQUE (user_id, achievement_id)
);
CREATE INDEX IF NOT EXISTS idx_sach_user_id ON student_achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_sach_achievement_id ON student_achievements(achievement_id);
CREATE INDEX IF NOT EXISTS idx_sach_earned_at ON student_achievements(earned_at);

CREATE TABLE IF NOT EXISTS badges (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  badge_type TEXT DEFAULT 'skill' CHECK (badge_type IN ('skill','completion','participation','special')),
  icon_url VARCHAR(500),
  background_color VARCHAR(7) DEFAULT '#1E40AF',
  text_color VARCHAR(7) DEFAULT '#FFFFFF',
  criteria JSONB NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_badge_type ON badges(badge_type);
CREATE INDEX IF NOT EXISTS idx_badge_is_active ON badges(is_active);

CREATE TABLE IF NOT EXISTS student_badges (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  badge_id BIGINT NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
  earned_at TIMESTAMPTZ DEFAULT NOW(),
  issued_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  evidence_data JSONB,
  CONSTRAINT unique_user_badge UNIQUE (user_id, badge_id)
);
CREATE INDEX IF NOT EXISTS idx_sbad_user_id ON student_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_sbad_badge_id ON student_badges(badge_id);

CREATE TABLE IF NOT EXISTS leaderboards (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  leaderboard_type TEXT DEFAULT 'global' CHECK (leaderboard_type IN ('global','course','game','custom')),
  scoring_method TEXT DEFAULT 'points' CHECK (scoring_method IN ('points','time','completion','custom')),
  time_period TEXT DEFAULT 'all_time' CHECK (time_period IN ('all_time','monthly','weekly','daily')),
  reset_frequency TEXT DEFAULT 'never' CHECK (reset_frequency IN ('never','daily','weekly','monthly','yearly')),
  max_entries INT DEFAULT 100,
  is_active BOOLEAN DEFAULT TRUE,
  config JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_lb_type ON leaderboards(leaderboard_type);
CREATE INDEX IF NOT EXISTS idx_lb_time_period ON leaderboards(time_period);

CREATE TABLE IF NOT EXISTS leaderboard_entries (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  leaderboard_id BIGINT NOT NULL REFERENCES leaderboards(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  score NUMERIC(15,2) NOT NULL DEFAULT 0,
  rank_position INT,
  metadata JSONB,
  last_updated TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT unique_leaderboard_user UNIQUE (leaderboard_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_lb_entries_user_id ON leaderboard_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_lb_entries_score ON leaderboard_entries(leaderboard_id, score DESC);

CREATE TABLE IF NOT EXISTS points_system (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  action_type VARCHAR(100) NOT NULL UNIQUE,
  points_value INT NOT NULL,
  description TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS student_points (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  total_points INT DEFAULT 0,
  available_points INT DEFAULT 0,
  spent_points INT DEFAULT 0,
  level INT DEFAULT 1,
  experience_points INT DEFAULT 0,
  last_updated TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT unique_user_points UNIQUE (user_id)
);
CREATE INDEX IF NOT EXISTS idx_sp_total_points ON student_points(total_points);
CREATE INDEX IF NOT EXISTS idx_sp_level ON student_points(level);

CREATE TABLE IF NOT EXISTS points_transactions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  transaction_type TEXT NOT NULL CHECK (transaction_type IN ('earned','spent','deducted','bonus')),
  points_amount INT NOT NULL,
  action_type VARCHAR(100),
  description TEXT,
  related_type TEXT CHECK (related_type IN ('course','game','assessment','achievement','system')),
  related_id BIGINT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_pt_user_id ON points_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_pt_transaction_type ON points_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_pt_created_at ON points_transactions(created_at);

-- =============================================
-- CONTENT MANAGEMENT TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS media_files (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  filename VARCHAR(255) NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  file_size BIGINT NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  file_type TEXT NOT NULL CHECK (file_type IN ('image','video','audio','document','archive','other')),
  alt_text VARCHAR(255),
  caption TEXT,
  metadata JSONB,
  uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  is_public BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_media_file_type ON media_files(file_type);
CREATE INDEX IF NOT EXISTS idx_media_uploaded_by ON media_files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_media_created_at ON media_files(created_at);

CREATE TABLE IF NOT EXISTS content_versions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  content_type TEXT NOT NULL CHECK (content_type IN ('course','lesson','assessment','game')),
  content_id BIGINT NOT NULL,
  version_number VARCHAR(20) NOT NULL,
  content_data JSONB NOT NULL,
  change_description TEXT,
  is_current BOOLEAN DEFAULT FALSE,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_cv_content_type_id ON content_versions(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_cv_is_current ON content_versions(is_current);

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  slug VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  color VARCHAR(7) DEFAULT '#6B7280',
  usage_count INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
CREATE INDEX IF NOT EXISTS idx_tags_slug ON tags(slug);

CREATE TABLE IF NOT EXISTS content_tags (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
  content_type TEXT NOT NULL CHECK (content_type IN ('course','lesson','game','assessment')),
  content_id BIGINT NOT NULL,
  tagged_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT unique_tag_content UNIQUE (tag_id, content_type, content_id)
);
CREATE INDEX IF NOT EXISTS idx_ct_tag_id ON content_tags(tag_id);
CREATE INDEX IF NOT EXISTS idx_ct_content_type_id ON content_tags(content_type, content_id);

-- =============================================
-- ANALYTICS & REPORTING TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS learning_analytics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  event_type VARCHAR(100) NOT NULL,
  event_data JSONB NOT NULL,
  session_id VARCHAR(255),
  ip_address VARCHAR(45),
  user_agent TEXT,
  referrer VARCHAR(500),
  timestamp TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_la_user_id ON learning_analytics(user_id);
CREATE INDEX IF NOT EXISTS idx_la_event_type ON learning_analytics(event_type);
CREATE INDEX IF NOT EXISTS idx_la_session_id ON learning_analytics(session_id);
CREATE INDEX IF NOT EXISTS idx_la_timestamp ON learning_analytics(timestamp);

CREATE TABLE IF NOT EXISTS engagement_metrics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content_type TEXT NOT NULL CHECK (content_type IN ('course','lesson','game','assessment','forum')),
  content_id BIGINT NOT NULL,
  metric_type TEXT NOT NULL CHECK (metric_type IN ('view','time_spent','interaction','completion')),
  metric_value NUMERIC(15,2) NOT NULL,
  date DATE NOT NULL,
  metadata JSONB,
  recorded_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_em_user_id ON engagement_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_em_content_type_id ON engagement_metrics(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_em_date ON engagement_metrics(date);
CREATE INDEX IF NOT EXISTS idx_em_metric_type ON engagement_metrics(metric_type);

CREATE TABLE IF NOT EXISTS performance_analytics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  assessment_type TEXT NOT NULL CHECK (assessment_type IN ('quiz','exam','game','assignment')),
  subject_area VARCHAR(100),
  score NUMERIC(5,2),
  max_score NUMERIC(5,2),
  completion_time INT,
  attempt_count INT DEFAULT 1,
  difficulty_level TEXT CHECK (difficulty_level IN ('easy','medium','hard')),
  date DATE NOT NULL,
  metadata JSONB
);
CREATE INDEX IF NOT EXISTS idx_pa_user_id ON performance_analytics(user_id);
CREATE INDEX IF NOT EXISTS idx_pa_assessment_type ON performance_analytics(assessment_type);
CREATE INDEX IF NOT EXISTS idx_pa_date ON performance_analytics(date);

CREATE TABLE IF NOT EXISTS system_logs (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  log_level TEXT NOT NULL CHECK (log_level IN ('DEBUG','INFO','WARNING','ERROR','CRITICAL')),
  message TEXT NOT NULL,
  module VARCHAR(100),
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  ip_address VARCHAR(45),
  user_agent TEXT,
  request_id VARCHAR(255),
  additional_data JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sl_log_level ON system_logs(log_level);
CREATE INDEX IF NOT EXISTS idx_sl_module ON system_logs(module);
CREATE INDEX IF NOT EXISTS idx_sl_user_id ON system_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_sl_created_at ON system_logs(created_at);

CREATE TABLE IF NOT EXISTS report_templates (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  report_type TEXT NOT NULL CHECK (report_type IN ('student_progress','course_analytics','game_performance','engagement','custom')),
  template_config JSONB NOT NULL,
  is_system_template BOOLEAN DEFAULT FALSE,
  is_active BOOLEAN DEFAULT TRUE,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_rt_report_type ON report_templates(report_type);
CREATE INDEX IF NOT EXISTS idx_rt_created_by ON report_templates(created_by);

CREATE TABLE IF NOT EXISTS custom_reports (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  template_id BIGINT REFERENCES report_templates(id) ON DELETE SET NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  parameters JSONB,
  generated_data JSONB,
  file_path VARCHAR(500),
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending','generating','completed','failed')),
  generated_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  generated_at TIMESTAMPTZ DEFAULT NOW(),
  expires_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_cr_template_id ON custom_reports(template_id);
CREATE INDEX IF NOT EXISTS idx_cr_status ON custom_reports(status);
CREATE INDEX IF NOT EXISTS idx_cr_generated_by ON custom_reports(generated_by);

-- =============================================
-- SYSTEM CONFIGURATION TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS settings (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  setting_key VARCHAR(100) NOT NULL UNIQUE,
  setting_value TEXT,
  setting_type TEXT DEFAULT 'string' CHECK (setting_type IN ('string','integer','boolean','json')),
  category VARCHAR(50) DEFAULT 'general',
  description TEXT,
  is_public BOOLEAN DEFAULT FALSE,
  updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_settings_category ON settings(category);
CREATE INDEX IF NOT EXISTS idx_settings_key ON settings(setting_key);

CREATE TABLE IF NOT EXISTS grade_scales (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  scale_data JSONB NOT NULL,
  is_default BOOLEAN DEFAULT FALSE,
  is_active BOOLEAN DEFAULT TRUE,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS integrations (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  integration_type VARCHAR(50) NOT NULL,
  config_data JSONB NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  last_sync TIMESTAMPTZ,
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_integrations_type ON integrations(integration_type);

-- =============================================
-- ADDITIONAL GODOT-SPECIFIC TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS godot_builds (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  build_version VARCHAR(50) NOT NULL,
  godot_version VARCHAR(20) NOT NULL,
  build_type TEXT DEFAULT 'development' CHECK (build_type IN ('development','staging','production')),
  platform TEXT NOT NULL CHECK (platform IN ('web','windows','mac','linux','android','ios')),
  file_path VARCHAR(500) NOT NULL,
  file_size BIGINT NOT NULL,
  checksum VARCHAR(255),
  is_active BOOLEAN DEFAULT FALSE,
  build_notes TEXT,
  uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_gb_game_id ON godot_builds(game_id);
CREATE INDEX IF NOT EXISTS idx_gb_build_version ON godot_builds(build_version);
CREATE INDEX IF NOT EXISTS idx_gb_platform ON godot_builds(platform);

CREATE TABLE IF NOT EXISTS game_configs (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  config_name VARCHAR(100) NOT NULL,
  config_data JSONB NOT NULL,
  is_default BOOLEAN DEFAULT FALSE,
  environment TEXT DEFAULT 'production' CHECK (environment IN ('development','staging','production')),
  created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT unique_game_config_env UNIQUE (game_id, config_name, environment)
);
CREATE INDEX IF NOT EXISTS idx_gc_game_id ON game_configs(game_id);

CREATE TABLE IF NOT EXISTS player_input_logs (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  session_id BIGINT NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
  input_type TEXT NOT NULL CHECK (input_type IN ('keyboard','mouse','touch','gamepad')),
  input_data JSONB NOT NULL,
  game_state JSONB,
  level_id BIGINT REFERENCES game_levels(id) ON DELETE SET NULL,
  timestamp TIMESTAMPTZ(3) DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_pil_session_id ON player_input_logs(session_id);
CREATE INDEX IF NOT EXISTS idx_pil_timestamp ON player_input_logs(timestamp);

CREATE TABLE IF NOT EXISTS game_state_snapshots (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  session_id BIGINT NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
  snapshot_type TEXT NOT NULL CHECK (snapshot_type IN ('periodic','checkpoint','death','completion','manual')),
  game_state JSONB NOT NULL,
  screenshot_data TEXT,
  performance_metrics JSONB,
  timestamp TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_gss_session_id ON game_state_snapshots(session_id);
CREATE INDEX IF NOT EXISTS idx_gss_snapshot_type ON game_state_snapshots(snapshot_type);
CREATE INDEX IF NOT EXISTS idx_gss_timestamp ON game_state_snapshots(timestamp);

CREATE TABLE IF NOT EXISTS multiplayer_sessions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  session_code VARCHAR(20) NOT NULL UNIQUE,
  host_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  max_players INT DEFAULT 4,
  current_players INT DEFAULT 1,
  session_status TEXT DEFAULT 'waiting' CHECK (session_status IN ('waiting','in_progress','paused','completed','abandoned')),
  session_data JSONB,
  started_at TIMESTAMPTZ DEFAULT NOW(),
  ended_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_ms_game_id ON multiplayer_sessions(game_id);
CREATE INDEX IF NOT EXISTS idx_ms_session_code ON multiplayer_sessions(session_code);
CREATE INDEX IF NOT EXISTS idx_ms_status ON multiplayer_sessions(session_status);

CREATE TABLE IF NOT EXISTS multiplayer_participants (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  session_id BIGINT NOT NULL REFERENCES multiplayer_sessions(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  joined_at TIMESTAMPTZ DEFAULT NOW(),
  left_at TIMESTAMPTZ,
  final_score INT DEFAULT 0,
  player_data JSONB,
  CONSTRAINT unique_session_user UNIQUE (session_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_mp_session_id ON multiplayer_participants(session_id);
CREATE INDEX IF NOT EXISTS idx_mp_user_id ON multiplayer_participants(user_id);

-- =============================================
-- TEXT SEARCH INDEXES (replacing MySQL FULLTEXT)
-- =============================================

-- Courses text search
CREATE INDEX IF NOT EXISTS idx_courses_search ON courses USING GIN (to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,'') || ' ' || coalesce(short_description,'')));
-- Games text search
CREATE INDEX IF NOT EXISTS idx_games_search ON games USING GIN (to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,'')));
-- Forum posts text search
CREATE INDEX IF NOT EXISTS idx_forum_posts_search ON forum_posts USING GIN (to_tsvector('simple', coalesce(content,'')));
-- Lessons text search
CREATE INDEX IF NOT EXISTS idx_lessons_search ON lessons USING GIN (to_tsvector('simple', coalesce(title,'')));

-- =============================================
-- TRIGGERS: updated_at auto-update
-- =============================================

DO $$ BEGIN
  CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_user_profiles_updated BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_roles_updated BEFORE UPDATE ON roles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_categories_updated BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_courses_updated BEFORE UPDATE ON courses FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_course_modules_updated BEFORE UPDATE ON course_modules FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_lessons_updated BEFORE UPDATE ON lessons FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_games_updated BEFORE UPDATE ON games FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_game_levels_updated BEFORE UPDATE ON game_levels FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_game_saves_updated BEFORE UPDATE ON game_saves FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_assessments_updated BEFORE UPDATE ON assessments FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_assessment_questions_updated BEFORE UPDATE ON assessment_questions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_learning_paths_updated BEFORE UPDATE ON learning_paths FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_forums_updated BEFORE UPDATE ON forums FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_forum_topics_updated BEFORE UPDATE ON forum_topics FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_forum_posts_updated BEFORE UPDATE ON forum_posts FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_announcements_updated BEFORE UPDATE ON announcements FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_achievements_updated BEFORE UPDATE ON achievements FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_badges_updated BEFORE UPDATE ON badges FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_leaderboards_updated BEFORE UPDATE ON leaderboards FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_points_system_updated BEFORE UPDATE ON points_system FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_report_templates_updated BEFORE UPDATE ON report_templates FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_settings_updated BEFORE UPDATE ON settings FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_grade_scales_updated BEFORE UPDATE ON grade_scales FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_integrations_updated BEFORE UPDATE ON integrations FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_game_configs_updated BEFORE UPDATE ON game_configs FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Composite indexes requested
CREATE INDEX IF NOT EXISTS idx_users_email_active ON users(email, is_active);
CREATE INDEX IF NOT EXISTS idx_courses_published_featured ON courses(is_published, is_featured);
CREATE INDEX IF NOT EXISTS idx_game_sessions_user_game ON game_sessions(user_id, game_id);
CREATE INDEX IF NOT EXISTS idx_student_progress_user_status ON student_progress(user_id, status);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_learning_analytics_user_event ON learning_analytics(user_id, event_type);
CREATE INDEX IF NOT EXISTS idx_engagement_metrics_content ON engagement_metrics(content_type, content_id, date);

-- End of schema

-- Triggers for last_updated columns
DO $$ BEGIN
  CREATE TRIGGER trg_student_points_last_updated BEFORE UPDATE ON student_points FOR EACH ROW EXECUTE FUNCTION set_last_updated();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_leaderboard_entries_last_updated BEFORE UPDATE ON leaderboard_entries FOR EACH ROW EXECUTE FUNCTION set_last_updated();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
