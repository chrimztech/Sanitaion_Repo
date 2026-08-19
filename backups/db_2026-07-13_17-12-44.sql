--
-- PostgreSQL database dump
--

\restrict roiZCsDBjQHccn8j6sigM45sZDj9eT4v2gNZb3A5bSO2N5Lqk7YRT8bhynFowgP

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: update_content_search_vector(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_content_search_vector() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.search_vector :=
    setweight(to_tsvector('english', coalesce(NEW.title,    '')), 'A') ||
    setweight(to_tsvector('english', coalesce(NEW.summary,  '')), 'B') ||
    setweight(to_tsvector('english', coalesce(NEW.keywords, '')), 'B') ||
    setweight(to_tsvector('english', coalesce(NEW.authors,  '')), 'C') ||
    setweight(to_tsvector('english', coalesce(NEW.body,     '')), 'D');
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_content_search_vector() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: approval_workflows; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.approval_workflows (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    published_at timestamp(6) with time zone,
    reviewed_at timestamp(6) with time zone,
    reviewer_comments text,
    status character varying(255) NOT NULL,
    submitted_at timestamp(6) with time zone NOT NULL,
    content_item_id uuid NOT NULL,
    published_by uuid,
    reviewed_by uuid,
    submitted_by uuid,
    CONSTRAINT approval_workflows_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'RETURNED'::character varying, 'APPROVED'::character varying, 'PUBLISHED'::character varying, 'ARCHIVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.approval_workflows OWNER TO postgres;

--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_logs (
    id uuid NOT NULL,
    action character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    entity_id character varying(255),
    entity_type character varying(255),
    ip_address character varying(255),
    new_value text,
    old_value text,
    user_agent character varying(255),
    user_id uuid
);


ALTER TABLE public.audit_logs OWNER TO postgres;

--
-- Name: bookmarks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bookmarks (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    content_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.bookmarks OWNER TO postgres;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    parent_id uuid
);


ALTER TABLE public.categories OWNER TO postgres;

--
-- Name: content_files; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.content_files (
    id uuid NOT NULL,
    access_level character varying(255) NOT NULL,
    checksum character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    file_extension character varying(255),
    file_path character varying(255) NOT NULL,
    file_size bigint,
    file_type character varying(255),
    mime_type character varying(255),
    original_file_name character varying(255) NOT NULL,
    stored_file_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version_number integer NOT NULL,
    content_item_id uuid NOT NULL,
    uploaded_by uuid,
    CONSTRAINT content_files_access_level_check CHECK (((access_level)::text = ANY ((ARRAY['PUBLIC'::character varying, 'UNIVERSITY'::character varying, 'STAFF'::character varying, 'STUDENT'::character varying, 'CORE_TEAM'::character varying, 'DRAFT'::character varying])::text[])))
);


ALTER TABLE public.content_files OWNER TO postgres;

--
-- Name: content_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.content_items (
    id uuid NOT NULL,
    access_level character varying(255) NOT NULL,
    activity_date date,
    authors text,
    bibliographic_info character varying(255),
    body text,
    content_type character varying(255) NOT NULL,
    copyright_status character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    deleted_at timestamp(6) with time zone,
    downloadable boolean NOT NULL,
    expires_at timestamp(6) with time zone,
    external_identifier character varying(255),
    keywords character varying(255),
    language character varying(255),
    licence_information character varying(255),
    publication_date date,
    published_at timestamp(6) with time zone,
    publisher character varying(255),
    slug character varying(255) NOT NULL,
    source_institution character varying(255),
    status character varying(255) NOT NULL,
    summary text,
    thumbnail_path character varying(255),
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    view_count integer NOT NULL,
    author_id uuid,
    category_id uuid,
    department_id uuid,
    search_vector tsvector,
    CONSTRAINT content_items_access_level_check CHECK (((access_level)::text = ANY ((ARRAY['PUBLIC'::character varying, 'UNIVERSITY'::character varying, 'STAFF'::character varying, 'STUDENT'::character varying, 'CORE_TEAM'::character varying, 'DRAFT'::character varying])::text[]))),
    CONSTRAINT content_items_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'RETURNED'::character varying, 'APPROVED'::character varying, 'PUBLISHED'::character varying, 'ARCHIVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.content_items OWNER TO postgres;

--
-- Name: content_tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.content_tags (
    content_item_id uuid NOT NULL,
    tag_id uuid NOT NULL
);


ALTER TABLE public.content_tags OWNER TO postgres;

--
-- Name: content_type_defs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.content_type_defs (
    id uuid NOT NULL,
    description text,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL
);


ALTER TABLE public.content_type_defs OWNER TO postgres;

--
-- Name: demonstration_sites; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.demonstration_sites (
    id uuid NOT NULL,
    contact_email character varying(255),
    contact_person character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    district character varying(255),
    latitude double precision,
    location character varying(255),
    longitude double precision,
    name character varying(255) NOT NULL,
    notes text,
    operational_since date,
    operational_status character varying(255),
    partner_institution character varying(255),
    site_type character varying(255),
    slug character varying(255) NOT NULL,
    technology character varying(255),
    updated_at timestamp(6) with time zone NOT NULL,
    image_path character varying(255)
);


ALTER TABLE public.demonstration_sites OWNER TO postgres;

--
-- Name: departments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.departments (
    id uuid NOT NULL,
    code character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    name character varying(255) NOT NULL,
    school_id uuid
);


ALTER TABLE public.departments OWNER TO postgres;

--
-- Name: digitization_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.digitization_records (
    id uuid NOT NULL,
    copyright_status character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    digitization_status character varying(255) NOT NULL,
    number_of_pages integer,
    ocr_text text,
    physical_location character varying(255),
    quality_status character varying(255),
    scanned_file_path character varying(255),
    source_owner character varying(255),
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    content_item_id uuid,
    reviewed_by uuid,
    scanned_by uuid,
    CONSTRAINT digitization_records_digitization_status_check CHECK (((digitization_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SCANNING'::character varying, 'SCANNED'::character varying, 'OCR_DONE'::character varying, 'METADATA_ADDED'::character varying, 'UNDER_REVIEW'::character varying, 'APPROVED'::character varying, 'PUBLISHED'::character varying, 'REJECTED'::character varying, 'RESCAN'::character varying])::text[])))
);


ALTER TABLE public.digitization_records OWNER TO postgres;

--
-- Name: downloads; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.downloads (
    id bigint NOT NULL,
    downloaded_at timestamp(6) with time zone NOT NULL,
    ip_address character varying(255),
    content_file_id uuid NOT NULL,
    user_id uuid
);


ALTER TABLE public.downloads OWNER TO postgres;

--
-- Name: downloads_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.downloads ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.downloads_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.events (
    id uuid NOT NULL,
    access_level character varying(255) NOT NULL,
    banner_image_path character varying(255),
    contact_email character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    end_date date,
    end_time time(6) without time zone,
    organizer character varying(255),
    published_at timestamp(6) with time zone,
    registration_link character varying(255),
    slug character varying(255) NOT NULL,
    start_date date NOT NULL,
    start_time time(6) without time zone,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    venue character varying(255),
    created_by uuid,
    department_id uuid,
    CONSTRAINT events_access_level_check CHECK (((access_level)::text = ANY ((ARRAY['PUBLIC'::character varying, 'UNIVERSITY'::character varying, 'STAFF'::character varying, 'STUDENT'::character varying, 'CORE_TEAM'::character varying, 'DRAFT'::character varying])::text[]))),
    CONSTRAINT events_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'RETURNED'::character varying, 'APPROVED'::character varying, 'PUBLISHED'::character varying, 'ARCHIVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.events OWNER TO postgres;

--
-- Name: hero_banners; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hero_banners (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    display_order integer,
    image_path character varying(255),
    link_url character varying(255),
    subtitle text,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.hero_banners OWNER TO postgres;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    link character varying(255),
    message text,
    read boolean NOT NULL,
    title character varying(255) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: reading_list_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reading_list_items (
    id uuid NOT NULL,
    added_at timestamp(6) with time zone NOT NULL,
    note text,
    "position" integer NOT NULL,
    content_id uuid NOT NULL,
    list_id uuid NOT NULL
);


ALTER TABLE public.reading_list_items OWNER TO postgres;

--
-- Name: reading_lists; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reading_lists (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    name character varying(255) NOT NULL,
    public_list boolean NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.reading_lists OWNER TO postgres;

--
-- Name: schools; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.schools (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.schools OWNER TO postgres;

--
-- Name: system_settings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.system_settings (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    setting_key character varying(255) NOT NULL,
    setting_value text,
    updated_at timestamp(6) with time zone NOT NULL,
    updated_by uuid
);


ALTER TABLE public.system_settings OWNER TO postgres;

--
-- Name: tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tags (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL
);


ALTER TABLE public.tags OWNER TO postgres;

--
-- Name: training_registrations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.training_registrations (
    id uuid NOT NULL,
    registered_at timestamp(6) with time zone NOT NULL,
    status character varying(255) NOT NULL,
    session_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.training_registrations OWNER TO postgres;

--
-- Name: training_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.training_sessions (
    id uuid NOT NULL,
    capacity integer,
    contact_email character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    end_date date,
    end_time time(6) without time zone,
    instructor character varying(255),
    mode character varying(255),
    published_at timestamp(6) with time zone,
    requirements text,
    slug character varying(255) NOT NULL,
    start_date date NOT NULL,
    start_time time(6) without time zone,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    venue character varying(255),
    created_by uuid,
    CONSTRAINT training_sessions_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'RETURNED'::character varying, 'APPROVED'::character varying, 'PUBLISHED'::character varying, 'ARCHIVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.training_sessions OWNER TO postgres;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    role character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT user_roles_role_check CHECK (((role)::text = ANY ((ARRAY['SUPER_ADMIN'::character varying, 'PROJECT_ADMIN'::character varying, 'CONTENT_MANAGER'::character varying, 'DIGITIZATION_OFFICER'::character varying, 'REVIEWER'::character varying, 'STAFF'::character varying, 'STUDENT'::character varying])::text[])))
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: user_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_sessions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    ip_address character varying(255),
    revoked_at timestamp(6) with time zone,
    session_id character varying(255) NOT NULL,
    user_agent character varying(500),
    user_id uuid NOT NULL
);


ALTER TABLE public.user_sessions OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    affiliation character varying(255),
    auth_type character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    institution character varying(255),
    job_title character varying(255),
    last_login_at timestamp(6) with time zone,
    password_hash character varying(255),
    phone character varying(255),
    reset_token character varying(255),
    reset_token_expires_at timestamp(6) with time zone,
    status character varying(255) NOT NULL,
    unza_id character varying(255),
    updated_at timestamp(6) with time zone NOT NULL,
    department_id uuid,
    school_id uuid,
    email_verification_token character varying(255),
    locked_until timestamp(6) with time zone,
    login_attempts integer DEFAULT 0 NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    current_session_id character varying(255),
    CONSTRAINT users_auth_type_check CHECK (((auth_type)::text = ANY ((ARRAY['LOCAL'::character varying, 'LDAP'::character varying, 'OAP'::character varying, 'HR'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: views; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.views (
    id bigint NOT NULL,
    ip_address character varying(255),
    viewed_at timestamp(6) with time zone NOT NULL,
    content_item_id uuid NOT NULL,
    user_id uuid
);


ALTER TABLE public.views OWNER TO postgres;

--
-- Name: views_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.views ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.views_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: approval_workflows; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.approval_workflows (id, created_at, published_at, reviewed_at, reviewer_comments, status, submitted_at, content_item_id, published_by, reviewed_by, submitted_by) FROM stdin;
e1e5ecfb-8317-4742-94af-38d281124152	2026-07-03 08:58:38.216786+02	\N	\N	\N	SUBMITTED	2026-07-03 08:58:38.213786+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N	\N	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
ae3b364d-39f8-4e38-a113-36c8a234b5aa	2026-07-03 08:58:42.930033+02	\N	2026-07-03 08:58:42.930034+02	\N	APPROVED	2026-07-03 08:58:42.930034+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
755e5a04-e3ae-4faa-8e99-28fc85593929	2026-07-03 08:58:45.426973+02	2026-07-03 08:58:45.425973+02	2026-07-03 08:58:45.425973+02	\N	PUBLISHED	2026-07-03 08:58:45.425973+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
14bf2cc0-112a-48ea-9562-438a10224fef	2026-07-03 13:19:26.334894+02	\N	\N	\N	SUBMITTED	2026-07-03 13:19:26.328904+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	\N	\N	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
370f73f9-8996-4513-a049-7e62abb22a9d	2026-07-03 13:19:36.051894+02	\N	2026-07-03 13:19:36.050896+02	\N	APPROVED	2026-07-03 13:19:36.050896+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	\N	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
729b8374-1a83-43db-9d57-dd4d18cae9c9	2026-07-03 13:19:39.429873+02	2026-07-03 13:19:39.429874+02	2026-07-03 13:19:39.429874+02	\N	PUBLISHED	2026-07-03 13:19:39.429874+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
0c5bf8e0-25f8-4026-ab6a-5abeb10c9c02	2026-07-13 17:06:20.048824+02	\N	\N	\N	SUBMITTED	2026-07-13 17:06:20.042833+02	c3957540-64d6-46c1-8032-4c89dc324923	\N	\N	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
8290d447-42f6-4774-b917-ed5919819fd9	2026-07-13 17:06:41.098107+02	\N	2026-07-13 17:06:41.097107+02	\N	APPROVED	2026-07-13 17:06:41.097107+02	c3957540-64d6-46c1-8032-4c89dc324923	\N	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
583383e4-9f6e-4f88-9967-ecaddb3a121b	2026-07-13 17:06:42.81269+02	2026-07-13 17:06:42.811688+02	2026-07-13 17:06:42.811688+02	\N	PUBLISHED	2026-07-13 17:06:42.811688+02	c3957540-64d6-46c1-8032-4c89dc324923	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.audit_logs (id, action, created_at, entity_id, entity_type, ip_address, new_value, old_value, user_agent, user_id) FROM stdin;
7081420c-2ff8-47b8-b8e5-1e890a8f6605	event.delete	2026-06-25 12:35:29.791657+02	f3fca930-e2ef-4561-a9b4-8a22c5ed2bb0	events	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
925e7a81-b0ed-488a-9787-bb2298ce38d2	event.delete	2026-06-25 12:35:32.796665+02	629cb646-4aae-400b-afbb-fd384c8fcf5c	events	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
9549d380-c5a0-410e-9a54-160f6093509b	event.delete	2026-06-25 12:35:35.55411+02	bef09f1f-e211-460d-b1b4-8d722ec21512	events	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
fa5ecece-a4e7-4f49-ba52-9acd58ded7d6	content.file.upload	2026-06-25 13:03:14.96291+02	e7090b24-fd94-42ea-983b-84f60f0bfce4	content_files	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
42af16e7-8919-40e1-8130-c964350b2cb2	user.roles.update	2026-06-25 13:18:59.067649+02	0e5b2cbb-c6bf-44bd-a299-d49493946804	users	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
b54eada4-4adf-4cdf-80d4-bf4eb044ccae	user.roles.update	2026-06-25 13:19:00.500164+02	0e5b2cbb-c6bf-44bd-a299-d49493946804	users	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
5498bc72-9792-4bfb-a675-24bca2d61c2b	user.roles.update	2026-06-25 13:19:01.48527+02	0e5b2cbb-c6bf-44bd-a299-d49493946804	users	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
554a964a-f1b7-4117-9829-909fa79d0342	user.roles.update	2026-06-25 13:19:02.347611+02	0e5b2cbb-c6bf-44bd-a299-d49493946804	users	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
e895f828-5902-4bc9-9bf4-2993ea15848f	site.update	2026-06-25 13:30:38.527315+02	d1e61b61-c630-4bfb-ac8c-aef1bc8cac28	demonstration_sites	\N	\N	\N	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804
a3c81021-6e51-41a6-a869-c4919028e226	training.create	2026-06-29 14:21:29.164932+02	6def3375-e9c8-4a7d-b1ec-7116dcca74e8	training_sessions	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
bc9cd14f-8922-4584-9d4b-25ba95f5c0ce	training.publish	2026-06-29 14:21:37.370056+02	6def3375-e9c8-4a7d-b1ec-7116dcca74e8	training_sessions	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
2bd84ac1-2f02-4976-8e1b-6226c509b7e1	training.delete	2026-06-29 14:27:29.963392+02	6def3375-e9c8-4a7d-b1ec-7116dcca74e8	training_sessions	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
c671721c-0eed-440d-8aca-94bac906e09a	digitization.create	2026-07-02 11:40:27.598614+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	0e5b2cbb-c6bf-44bd-a299-d49493946804
48eed15c-7e97-45e8-acd9-bc44f6d53ef0	digitization.update	2026-07-02 11:41:07.736037+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	0e5b2cbb-c6bf-44bd-a299-d49493946804
b09da1b0-1553-47cf-8b25-109679ad2822	digitization.update	2026-07-02 13:57:34.026662+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
ff71a7a8-d8d3-4bcb-98b4-86ea674deaaa	digitization.scan.upload	2026-07-02 14:02:49.05203+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
7ba0afd7-f117-40a0-83f8-45994fcdc737	digitization.scan.upload	2026-07-02 14:03:24.493503+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
0c04b50a-b17d-41b8-93e7-8940769f687d	digitization.status.ocr_done	2026-07-02 14:13:02.240899+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
80dcb272-ac7c-4e12-afb0-4069a7daa884	digitization.status.metadata_added	2026-07-02 14:13:09.943394+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
e4634e30-37f8-4c67-9ab3-f0b738ac581b	digitization.status.under_review	2026-07-02 14:13:14.837602+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
81b0caa3-5d4d-44ef-8d7e-466090e5506b	digitization.status.approved	2026-07-02 14:13:18.598253+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
b6a7d31a-4e1d-4a09-935d-462bb22c8e13	digitization.status.published	2026-07-02 14:13:23.627393+02	16759e29-24cc-4db9-b44c-5fca250f7cb9	digitization_records	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
2d4d3cb9-51da-40a5-9895-ecb2bd58333b	user.roles.update	2026-07-02 16:24:44.24284+02	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
c57d53c7-9b43-434b-b739-c7d4e3bfba1d	user.roles.update	2026-07-02 16:24:45.268696+02	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
53ebd7b6-f42c-4603-9e75-9d2ef386c066	user.roles.update	2026-07-02 16:24:48.589454+02	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
81d231b8-2b5d-4344-9c32-862409db6ff2	user.roles.update	2026-07-02 16:24:49.95484+02	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
a319efa0-9ce5-40bb-b180-8e92f87c2cf6	user.roles.update	2026-07-02 16:24:51.549658+02	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
02badd4b-4c3f-4fad-bdc3-44ed26a54030	user.roles.update	2026-07-02 16:24:52.883751+02	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
c2d39617-9dab-408b-b856-9362654268b3	taxonomy.category.create	2026-07-03 08:55:35.518545+02	a6482048-62da-4d43-829a-ad1bd28f594f	categories	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
69469cdd-8ba3-4359-9c59-8d66d43a3e23	taxonomy.category.create	2026-07-03 08:55:46.779212+02	6da45f1e-565f-4b09-85b1-c3fdf1d2e581	categories	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
a8f93066-5363-41eb-a40f-a9bc22693aa1	taxonomy.content_type.create	2026-07-03 08:55:56.343624+02	7a7a6346-5508-4683-9bc1-46cecf59c38d	content_type_defs	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
34bead8c-e245-4e21-a26d-edc89f39607d	taxonomy.content_type.create	2026-07-03 08:56:04.413497+02	25f81be2-9e8c-4435-8657-edd3ef8d6bd2	content_type_defs	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
ab3f58ba-518c-4bc1-afd4-1b868e78cb4f	taxonomy.content_type.create	2026-07-03 08:56:13.227987+02	f79a9235-d855-4b10-a88a-d6cd70e6488f	content_type_defs	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
33b02c77-5c89-4f25-9bd5-6de1e9a3d79d	content.create	2026-07-03 08:57:58.634943+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
bf3896af-fdcf-4a13-bac4-b6f105cf0ba1	content.file.upload	2026-07-03 08:58:29.502921+02	ec7b07f4-20b0-4e51-b607-9ff1041fe1bb	content_files	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
393fd1a9-e635-460e-9b24-e41dadc31484	content.update	2026-07-03 08:58:33.484547+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
702c79f0-d7a7-4ccd-8535-c91caef5be49	content.submit	2026-07-03 08:58:38.217785+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
b2ff1d55-4fbd-46f9-b02f-b9d4563ad1ac	content.approve	2026-07-03 08:58:42.930033+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
e60c453e-01b9-4d90-aacc-729d34e16fde	content.publish	2026-07-03 08:58:45.427974+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
08fe1d00-8601-4aaf-aaf7-922c05bb32ff	content.update	2026-07-03 09:18:58.017823+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
34c6c6cc-2395-4072-bfa7-2589305870af	content.update	2026-07-03 09:21:10.285949+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
af7ed714-9a76-43ee-bd81-69b85f1d1bfb	content.create	2026-07-03 13:18:01.265354+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
6704efc7-a157-4724-a9a4-110e2240e114	content.file.upload	2026-07-03 13:19:21.866137+02	449b6367-49a1-4942-9a7c-ed6bd21672ff	content_files	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
3ba193cd-a048-4af5-b20e-48c1b2cf7bcb	content.submit	2026-07-03 13:19:26.334894+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
959e7453-21ce-48a2-8e88-c9daca5fd6b1	content.approve	2026-07-03 13:19:36.051894+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
bc51272c-5a21-440c-8a67-1146e5e1b94d	content.publish	2026-07-03 13:19:39.430872+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
e7a82c9e-c850-442a-be39-5a4b029ace53	site.create	2026-07-03 15:19:57.359048+02	b901bd65-fa6e-441d-989f-4d68275f3919	demonstration_sites	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
b44cf033-db5f-4b26-a4c6-dfbec013183e	site.update	2026-07-03 16:24:27.67398+02	b901bd65-fa6e-441d-989f-4d68275f3919	demonstration_sites	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
08e37b0c-1863-413e-a89d-3cc4daeef5ca	site.delete	2026-07-03 16:39:54.161837+02	b901bd65-fa6e-441d-989f-4d68275f3919	demonstration_sites	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
93212503-6a68-4095-8468-7240ef65b17e	training.create	2026-07-09 14:26:58.816578+02	eb6c0702-fb3b-4130-886c-bc52df13380d	training_sessions	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
9f85bd94-59c2-4551-a589-ba084ad40cab	training.publish	2026-07-09 14:27:29.066147+02	eb6c0702-fb3b-4130-886c-bc52df13380d	training_sessions	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
b75fc749-d74d-41a2-85f9-88ac5a9bb75c	site.create	2026-07-09 21:25:23.705627+02	c1c5af6a-0ef6-4c5b-877b-1c773b348ed8	demonstration_sites	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
c0693300-a3ae-4928-946c-ce672874b318	site.update	2026-07-09 21:27:23.204924+02	c1c5af6a-0ef6-4c5b-877b-1c773b348ed8	demonstration_sites	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
0f26d3ef-d633-4d8a-87e1-57f812a341ba	site.delete	2026-07-09 21:27:38.290637+02	c1c5af6a-0ef6-4c5b-877b-1c773b348ed8	demonstration_sites	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
c38ac1ee-39be-46fb-bf70-aa4d25449425	training.delete	2026-07-09 21:29:03.440067+02	eb6c0702-fb3b-4130-886c-bc52df13380d	training_sessions	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
5a2871a3-330a-4c8f-b872-f30f7decfc35	taxonomy.content_types.delete	2026-07-09 21:32:11.323144+02	f79a9235-d855-4b10-a88a-d6cd70e6488f	content_types	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
855fe90d-16c8-4cc7-8903-0821983c366e	taxonomy.category.create	2026-07-10 08:49:38.716987+02	45c1f80e-c47b-4f62-9ae6-3c4647ccc6b4	categories	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
aa89e415-7339-455a-aaaf-b1462700d190	taxonomy.content_type.create	2026-07-10 08:49:56.710095+02	c35c3fa7-8a93-45bb-b90e-ae88b37ca3db	content_type_defs	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
668f2438-c856-466a-9369-384c75902845	taxonomy.department.create	2026-07-10 08:50:18.137046+02	779528a6-8171-4185-9c8c-f5f1a3cb0d77	departments	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
31d0423c-dbcf-4261-b31f-9b54f0ddcb25	taxonomy.departments.delete	2026-07-10 08:50:20.368283+02	779528a6-8171-4185-9c8c-f5f1a3cb0d77	departments	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
69afc80b-f68f-48a3-a74c-70b5e826dfeb	user.roles.update	2026-07-13 16:57:53.775448+02	4b846d3f-ad1e-4732-aed4-d57a956c49d7	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
226f4610-1295-46e5-ada4-9daa655eaf9c	user.roles.update	2026-07-13 16:57:57.536599+02	4b846d3f-ad1e-4732-aed4-d57a956c49d7	users	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
c2eed240-c091-4fc7-9b6a-119b7ec7e034	content.create	2026-07-13 17:05:47.716758+02	c3957540-64d6-46c1-8032-4c89dc324923	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
04f3a1d5-ec80-4fe0-9d92-6e52bd7a43c4	content.file.upload	2026-07-13 17:06:10.206187+02	aec8bc24-d978-43a4-bd06-811435e0715d	content_files	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
039185cb-ac6d-4bb5-8949-d013dec4bacb	content.submit	2026-07-13 17:06:20.049827+02	c3957540-64d6-46c1-8032-4c89dc324923	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
bd9de537-8d53-4145-b15c-eb7404063506	content.approve	2026-07-13 17:06:41.098107+02	c3957540-64d6-46c1-8032-4c89dc324923	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
3a4a3d46-e918-4df7-91e4-ea606d9f7977	content.publish	2026-07-13 17:06:42.81269+02	c3957540-64d6-46c1-8032-4c89dc324923	content_items	0:0:0:0:0:0:0:1	\N	\N	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Data for Name: bookmarks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.bookmarks (id, created_at, content_id, user_id) FROM stdin;
e7cd63b2-536a-4b97-b94b-17f689384960	2026-07-03 14:07:55.218916+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	4b846d3f-ad1e-4732-aed4-d57a956c49d7
59279897-4e3b-49d6-a885-637af7806bdb	2026-07-03 14:08:03.145555+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
4b7ae4e3-37b6-45c0-a946-32a7823b1758	2026-07-13 17:01:30.794673+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categories (id, created_at, description, name, slug, parent_id) FROM stdin;
a6482048-62da-4d43-829a-ad1bd28f594f	2026-07-03 08:55:35.517554+02		Article	article	\N
6da45f1e-565f-4b09-85b1-c3fdf1d2e581	2026-07-03 08:55:46.779212+02		Journal	journal	\N
45c1f80e-c47b-4f62-9ae6-3c4647ccc6b4	2026-07-10 08:49:38.698956+02		Report	report	\N
\.


--
-- Data for Name: content_files; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.content_files (id, access_level, checksum, created_at, file_extension, file_path, file_size, file_type, mime_type, original_file_name, stored_file_name, updated_at, version_number, content_item_id, uploaded_by) FROM stdin;
ec7b07f4-20b0-4e51-b607-9ff1041fe1bb	PUBLIC	2703757c7453948019f759d43b5d3610abd38c5b0b5cfac2d785b31bd2cad58d	2026-07-03 08:58:29.501917+02	pdf	content-files/10bccde4-31bf-42e6-8799-4d2500c1d14b/b25eeabc-61d9-4a0c-8e4a-7f3fb5b0882e.pdf	9033222	application	application/pdf	Java A Beginner's Guide, 6th Edition (PDF).pdf	b25eeabc-61d9-4a0c-8e4a-7f3fb5b0882e.pdf	2026-07-03 08:58:29.501917+02	1	10bccde4-31bf-42e6-8799-4d2500c1d14b	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
449b6367-49a1-4942-9a7c-ed6bd21672ff	PUBLIC	8b5cfb151bb11be0f21211382de01e96990526b49e7fa0710ae08749fd5aadd9	2026-07-03 13:19:21.864137+02	docx	content-files/ad01694d-68dc-4b18-9ab5-37e89f5b5f79/5a3cd6b8-ccd2-4420-8e69-4c094b61b040.docx	250972	application	application/vnd.openxmlformats-officedocument.wordprocessingml.document	Mubanga.docx	5a3cd6b8-ccd2-4420-8e69-4c094b61b040.docx	2026-07-03 13:19:21.864137+02	1	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
aec8bc24-d978-43a4-bd06-811435e0715d	PUBLIC	1db75c1fde8445cc7a9c7882cc91304f41d8a2f3ae2f227571140cd7a4c54e26	2026-07-13 17:06:10.196172+02	pdf	content-files/c3957540-64d6-46c1-8032-4c89dc324923/4ea96198-8aa5-42b0-8844-131752ad09e5.pdf	12020	application	application/pdf	Python_Short_Course_Outline.pdf	4ea96198-8aa5-42b0-8844-131752ad09e5.pdf	2026-07-13 17:06:10.196172+02	1	c3957540-64d6-46c1-8032-4c89dc324923	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Data for Name: content_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.content_items (id, access_level, activity_date, authors, bibliographic_info, body, content_type, copyright_status, created_at, deleted_at, downloadable, expires_at, external_identifier, keywords, language, licence_information, publication_date, published_at, publisher, slug, source_institution, status, summary, thumbnail_path, title, updated_at, view_count, author_id, category_id, department_id, search_vector) FROM stdin;
c3957540-64d6-46c1-8032-4c89dc324923	PUBLIC	\N	rtyuioiuy			report		2026-07-13 17:05:47.715757+02	\N	t	\N		huiuytyui	en		\N	2026-07-13 17:06:42.811688+02	ghjuiuy	dfghjuitdfg		PUBLISHED	sdfghjkopoiuytrfgyiop[09	thumbnails/c3957540-64d6-46c1-8032-4c89dc324923/thumb.jpg	dfghjuitdfg	2026-07-13 17:07:00.082882+02	1	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	\N	\N	'09':3B 'dfghjuitdfg':1A 'huiuytyui':4B 'rtyuioiuy':5C 'sdfghjkopoiuytrfgyiop':2B
ad01694d-68dc-4b18-9ab5-37e89f5b5f79	PUBLIC	\N				report		2026-07-03 13:18:01.263353+02	\N	t	\N			en		\N	2026-07-03 13:19:39.428873+02		test-file		PUBLISHED		\N	test file	2026-07-13 17:00:44.070171+02	6	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	\N	\N	'file':2A 'test':1A
10bccde4-31bf-42e6-8799-4d2500c1d14b	PUBLIC	2026-07-04	matakala 		An Introduction to Java 	pdf		2026-07-03 08:57:58.63293+02	\N	t	\N		java, 	en		2026-07-22	2026-07-03 08:58:45.425973+02	unza 	introduction-to-java		PUBLISHED		thumbnails/10bccde4-31bf-42e6-8799-4d2500c1d14b/thumb.jpg	Introduction to Java	2026-07-13 17:01:27.321143+02	26	67dd7cab-0b3f-4223-828a-d0d2e5b175ba	\N	\N	'introduct':1A,7 'java':3A,4B,9 'matakala':5C
\.


--
-- Data for Name: content_tags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.content_tags (content_item_id, tag_id) FROM stdin;
\.


--
-- Data for Name: content_type_defs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.content_type_defs (id, description, name, slug) FROM stdin;
7a7a6346-5508-4683-9bc1-46cecf59c38d		PDF	pdf
25f81be2-9e8c-4435-8657-edd3ef8d6bd2		VIDEO	video
c35c3fa7-8a93-45bb-b90e-ae88b37ca3db		PPT	ppt
\.


--
-- Data for Name: demonstration_sites; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.demonstration_sites (id, contact_email, contact_person, created_at, description, district, latitude, location, longitude, name, notes, operational_since, operational_status, partner_institution, site_type, slug, technology, updated_at, image_path) FROM stdin;
\.


--
-- Data for Name: departments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.departments (id, code, created_at, name, school_id) FROM stdin;
\.


--
-- Data for Name: digitization_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.digitization_records (id, copyright_status, created_at, description, digitization_status, number_of_pages, ocr_text, physical_location, quality_status, scanned_file_path, source_owner, title, updated_at, content_item_id, reviewed_by, scanned_by) FROM stdin;
16759e29-24cc-4db9-b44c-5fca250f7cb9	public_domain	2026-07-02 11:40:27.566621+02	fghjkl;kjhgfdfghjkl;kjhgf	PUBLISHED	200	fghjklkhgffdvbnkljbvcvbkljhgfdfghjkljhgfghjk;'\n\nhgfghkp[];lokiuydfghjk[]piyhrtguio[\n\njhgfsdfghjk;';lkjhgfrghjl;'\niugfdsdfghjkl;lkjhgfdfghjl;		excellent	digitization-files/16759e29-24cc-4db9-b44c-5fca250f7cb9/0b0116c2-56b8-4c00-9e2d-9e057b009655.pdf	wertyui	dfghjkkjhgfddfgh	2026-07-02 14:13:23.629394+02	\N	0e5b2cbb-c6bf-44bd-a299-d49493946804	0e5b2cbb-c6bf-44bd-a299-d49493946804
\.


--
-- Data for Name: downloads; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.downloads (id, downloaded_at, ip_address, content_file_id, user_id) FROM stdin;
1	2026-07-03 10:03:39.831856+02	0:0:0:0:0:0:0:1	ec7b07f4-20b0-4e51-b607-9ff1041fe1bb	4b846d3f-ad1e-4732-aed4-d57a956c49d7
2	2026-07-03 13:20:56.078343+02	0:0:0:0:0:0:0:1	449b6367-49a1-4942-9a7c-ed6bd21672ff	4b846d3f-ad1e-4732-aed4-d57a956c49d7
3	2026-07-13 17:00:53.614238+02	0:0:0:0:0:0:0:1	449b6367-49a1-4942-9a7c-ed6bd21672ff	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.events (id, access_level, banner_image_path, contact_email, created_at, description, end_date, end_time, organizer, published_at, registration_link, slug, start_date, start_time, status, title, updated_at, venue, created_by, department_id) FROM stdin;
\.


--
-- Data for Name: hero_banners; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.hero_banners (id, active, created_at, display_order, image_path, link_url, subtitle, title, updated_at) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, created_at, link, message, read, title, user_id) FROM stdin;
9a61140e-2fcf-480d-81f6-1d9130b9b5b4	2026-06-25 14:21:52.871393+02	\N	Your account has been created successfully. You can now browse resources, bookmark content, and engage with the platform.	f	Welcome to Sanitation Hub	4d0c1078-4b0c-4e74-aa10-ec5157366eb4
c5170bc7-05ce-49be-8ed9-ba7c80a35694	2026-06-25 16:01:53.511747+02	\N	Signed in with your UNZA staff credentials. Contact an administrator to adjust your access level.	f	Welcome to the Sanitation Hub	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
e379aaea-9dfc-493a-a55c-b93c51432794	2026-06-25 16:03:43.962619+02	\N	Signed in with your UNZA student credentials. You can now access resources and bookmark content.	f	Welcome to the Sanitation Hub	4b846d3f-ad1e-4732-aed4-d57a956c49d7
\.


--
-- Data for Name: reading_list_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reading_list_items (id, added_at, note, "position", content_id, list_id) FROM stdin;
\.


--
-- Data for Name: reading_lists; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reading_lists (id, created_at, description, name, public_list, updated_at, user_id) FROM stdin;
\.


--
-- Data for Name: schools; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.schools (id, created_at, name) FROM stdin;
\.


--
-- Data for Name: system_settings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.system_settings (id, created_at, description, setting_key, setting_value, updated_at, updated_by) FROM stdin;
d357a03b-c861-4f2e-8469-f5e0cae5d002	2026-06-25 04:07:11.273455+02	Display name	system_name	"UNZA Sanitation Hub & Laboratory Repository"	2026-06-25 04:07:11.273455+02	\N
9ba4e0c0-e19c-4375-b705-07a01e84448d	2026-06-25 04:07:11.283462+02	Short system identifier	system_abbreviation	"SANITATION-HUB"	2026-06-25 04:07:11.283462+02	\N
a6b4895a-fc47-4314-b57e-8c55b3cb7ae6	2026-06-25 04:07:11.291454+02	Max single-file upload in MB	max_upload_mb	500	2026-06-25 04:07:11.291454+02	\N
d18d51ed-dece-49e3-894e-52642bc4c5b4	2026-06-25 04:07:11.300454+02	Default access level for new content	default_access_level	"draft"	2026-06-25 04:07:11.300454+02	\N
6925d541-8957-4790-a945-0354b4ff6b25	2026-06-25 04:07:11.307457+02	Public contact email	contact_email	"sanitation@unza.zm"	2026-06-25 04:07:11.307457+02	\N
72d7fccd-2d78-47ed-bf70-9672ea6ad2e7	2026-06-25 04:07:11.314454+02	Allow general public to register accounts	allow_public_registration	true	2026-06-25 04:07:11.315452+02	\N
fca1e938-3edf-4faa-bc83-90e47cfe5e00	2026-06-25 04:07:11.324458+02	Require admin approval before content goes live	require_approval_before_publish	true	2026-06-25 04:07:11.324458+02	\N
dc35d14d-dd38-49f0-9346-c78fed68aff2	2026-06-25 04:07:11.332453+02	Authenticated users only can download files	download_requires_login	true	2026-06-25 04:07:11.332453+02	\N
\.


--
-- Data for Name: tags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tags (id, created_at, name, slug) FROM stdin;
\.


--
-- Data for Name: training_registrations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.training_registrations (id, registered_at, status, session_id, user_id) FROM stdin;
\.


--
-- Data for Name: training_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.training_sessions (id, capacity, contact_email, created_at, description, end_date, end_time, instructor, mode, published_at, requirements, slug, start_date, start_time, status, title, updated_at, venue, created_by) FROM stdin;
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (id, created_at, role, user_id) FROM stdin;
f6d65963-55c4-4b77-b176-214b16aea180	2026-06-25 13:19:02.345612+02	PROJECT_ADMIN	0e5b2cbb-c6bf-44bd-a299-d49493946804
a6f818e7-2d98-46f2-8b19-544dac88c168	2026-06-25 13:19:02.345612+02	STAFF	0e5b2cbb-c6bf-44bd-a299-d49493946804
1c971dfc-46f9-432e-92c3-2c2aa1708ec2	2026-06-25 13:19:02.346609+02	CONTENT_MANAGER	0e5b2cbb-c6bf-44bd-a299-d49493946804
34e49faf-66e1-4460-9b1c-cdcafa117d48	2026-06-25 13:19:02.346609+02	SUPER_ADMIN	0e5b2cbb-c6bf-44bd-a299-d49493946804
3ed34e2e-9850-43af-b0a6-9e800e3c6fa1	2026-06-25 13:19:02.346609+02	DIGITIZATION_OFFICER	0e5b2cbb-c6bf-44bd-a299-d49493946804
9b579d81-7faf-49e7-b0cb-f887ac7820b3	2026-06-25 13:19:02.346609+02	REVIEWER	0e5b2cbb-c6bf-44bd-a299-d49493946804
59268dbf-153c-44f3-bb3e-a89b798a7c8b	2026-06-25 13:19:02.346609+02	STUDENT	0e5b2cbb-c6bf-44bd-a299-d49493946804
ab5359ed-bfd2-46fa-8792-3ccab29fcbe3	2026-06-25 14:21:52.8644+02	STUDENT	4d0c1078-4b0c-4e74-aa10-ec5157366eb4
479dc3be-9151-42b5-8179-1db59f9fadd4	2026-07-02 16:24:52.881231+02	CONTENT_MANAGER	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
cf212107-5169-46e6-87c4-f8dacae2feb0	2026-07-02 16:24:52.881231+02	STAFF	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
b91107a3-5623-435b-bf38-92bc75f9bf46	2026-07-02 16:24:52.882739+02	DIGITIZATION_OFFICER	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
849ebf63-4872-4912-8b30-149c3cb2926b	2026-07-02 16:24:52.882739+02	PROJECT_ADMIN	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
cfba2d3c-bab0-4428-876e-1d58cd670ba2	2026-07-02 16:24:52.882739+02	REVIEWER	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
230e8f2f-6102-4939-a397-2be28641f835	2026-07-02 16:24:52.882739+02	SUPER_ADMIN	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
ca62702e-09ce-45e2-9003-cc371dcd3448	2026-07-02 16:24:52.883751+02	STUDENT	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
345cc694-6327-42e2-ad32-7a640f1edce2	2026-07-13 16:57:57.536599+02	STUDENT	4b846d3f-ad1e-4732-aed4-d57a956c49d7
e1cc0423-2039-4912-bd8c-de3bd85261c9	2026-07-13 16:57:57.536599+02	REVIEWER	4b846d3f-ad1e-4732-aed4-d57a956c49d7
\.


--
-- Data for Name: user_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_sessions (id, created_at, expires_at, ip_address, revoked_at, session_id, user_agent, user_id) FROM stdin;
5324faae-9e07-4933-9e46-d2b8b8499e1a	2026-07-02 13:54:52.563585+02	2026-07-03 01:54:52.512909+02	0:0:0:0:0:0:0:1	2026-07-02 14:07:12.900003+02	7fde5f4e-1bc4-438a-b4c4-fed0f25fc73f	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
20c8ff01-80fc-4e1b-b48f-2f5ceb3bc1ec	2026-07-02 14:07:19.266721+02	2026-07-03 02:07:19.265723+02	0:0:0:0:0:0:0:1	2026-07-02 14:12:51.466897+02	ec028902-60f7-4576-84e0-5b88f87b4d86	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
c4c8acb2-abe6-4e7f-be73-848be8f79257	2026-07-02 14:12:51.648824+02	2026-07-03 02:12:51.47542+02	0:0:0:0:0:0:0:1	2026-07-02 15:25:16.380469+02	a9cf1989-d9a8-4001-955b-29ab9770121b	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
73868543-2036-4b60-b62f-de626e1bd8f5	2026-07-02 15:25:16.540891+02	2026-07-03 03:25:16.386016+02	0:0:0:0:0:0:0:1	2026-07-02 16:16:12.954548+02	280f1837-3aa5-478e-a222-e2ffd7a7533b	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
0a8c848b-b7a3-4361-abe2-cc13cbe4981a	2026-07-02 16:22:13.586662+02	2026-07-03 04:22:13.57812+02	0:0:0:0:0:0:0:1	2026-07-02 16:23:10.965148+02	cfe34022-7668-462e-9663-ce2f014f82e5	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
5eed4dc6-5d71-4664-a6e5-782755d2484b	2026-07-02 16:24:34.36253+02	2026-07-03 04:24:34.360535+02	0:0:0:0:0:0:0:1	2026-07-02 16:25:18.050961+02	2b020275-e12c-41f4-8517-c29c1ffbbd60	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	0e5b2cbb-c6bf-44bd-a299-d49493946804
08b1c9b2-7571-4424-984b-fb638fe6f907	2026-07-02 16:26:02.073778+02	2026-07-03 04:26:02.070782+02	0:0:0:0:0:0:0:1	2026-07-02 16:26:42.456502+02	37241e08-a6de-4a2d-8e25-e8aacc04eb5d	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
28d176ce-08a9-45ac-b1d4-81eebf8da735	2026-07-02 16:27:05.781577+02	2026-07-03 04:27:05.779578+02	0:0:0:0:0:0:0:1	2026-07-03 08:50:17.976212+02	e3d1264a-e411-4f0c-9a57-83f5fa4577cd	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
ee86f29b-6407-4460-b3f3-7c7fb1a37963	2026-07-03 08:50:17.983205+02	2026-07-03 20:50:17.977211+02	0:0:0:0:0:0:0:1	2026-07-03 08:59:26.252572+02	4806ecb9-fd38-4141-86e1-c0758b197fc6	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
d37d7938-ff48-4f5d-8093-94878ba5c989	2026-07-03 09:00:00.123877+02	2026-07-03 21:00:00.121361+02	0:0:0:0:0:0:0:1	2026-07-03 09:04:33.227884+02	8e8098a0-3e77-4471-be66-593bf63199c1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
a4dbe43e-3289-4168-8a94-8b3b49c39494	2026-07-03 09:14:02.062843+02	2026-07-03 21:14:02.060843+02	0:0:0:0:0:0:0:1	2026-07-03 09:15:10.69159+02	0f2ec4e9-3060-4b9c-8482-764f8ba7b42f	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
e28bbcf5-abb4-4de6-8dc9-07570d9876c0	2026-07-03 09:15:17.156559+02	2026-07-03 21:15:17.15556+02	0:0:0:0:0:0:0:1	2026-07-03 09:18:11.458056+02	4e3efe51-5366-40f2-a714-d7283209354f	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
03eb713e-dbd6-432c-bcec-f37bc68bf831	2026-07-03 09:18:44.617609+02	2026-07-03 21:18:44.574476+02	0:0:0:0:0:0:0:1	2026-07-03 09:19:38.39219+02	09f1f43c-ee2d-4550-bef7-f50ad49da036	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
7de67f52-e095-4d4f-a37a-3f829485e855	2026-07-03 09:19:43.301546+02	2026-07-03 21:19:43.297545+02	0:0:0:0:0:0:0:1	2026-07-03 09:20:43.461587+02	7b019d79-fc2f-49ae-a823-80435a462832	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
74b3fd34-1842-49c3-a7c8-c55fe036acd6	2026-07-03 09:20:48.870421+02	2026-07-03 21:20:48.867412+02	0:0:0:0:0:0:0:1	2026-07-03 09:21:15.161984+02	68aa86c6-1c13-4aeb-ae0e-04cce6bc7e7b	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
57917cf6-dadb-4ba7-b20a-f92a9767374f	2026-07-03 09:21:23.107865+02	2026-07-03 21:21:23.106865+02	0:0:0:0:0:0:0:1	2026-07-03 09:25:13.715289+02	9a042ea9-b31c-4acb-adfa-7fca5c7d66ed	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
421ae8b3-1887-40d8-ae36-6d37e7ee6082	2026-07-03 09:25:22.760172+02	2026-07-03 21:25:22.72329+02	0:0:0:0:0:0:0:1	2026-07-03 09:32:22.194696+02	31069554-16b5-499d-8347-198512ff8e2e	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
0149c2b9-82f0-48e4-8813-9b306f6fa65b	2026-07-03 09:32:28.090297+02	2026-07-03 21:32:28.043661+02	0:0:0:0:0:0:0:1	2026-07-03 09:36:09.613759+02	cf5231ea-6ddb-4e81-a5c6-af99dd15113b	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
402db013-d332-4f23-b713-d11e1297da41	2026-07-03 09:36:17.873345+02	2026-07-03 21:36:17.843751+02	0:0:0:0:0:0:0:1	2026-07-03 09:47:05.1109+02	fc2a0bee-7bfc-46e6-af34-48646df23d98	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
39abfc68-aabb-48c1-9e4f-8179d7e18d50	2026-07-03 09:47:14.023227+02	2026-07-03 21:47:13.997186+02	0:0:0:0:0:0:0:1	2026-07-03 10:02:40.934547+02	30e5050d-455b-4e48-a410-2a68eb4d75c9	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
d4901a96-d96d-4b4b-bace-f0bffefe2245	2026-07-03 10:02:50.905731+02	2026-07-03 22:02:50.877684+02	0:0:0:0:0:0:0:1	2026-07-03 13:16:29.556153+02	f4d456c7-a248-47d4-b15a-a9c7903e761a	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
46667650-1ce5-4723-bdca-0abc100eedb9	2026-07-03 13:17:29.541034+02	2026-07-04 01:17:29.537022+02	0:0:0:0:0:0:0:1	2026-07-03 13:19:51.231888+02	1d795b0b-34d6-4c08-888b-b5fd773e6694	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
afbbd690-1a0e-459d-86ec-8320a00ee0b0	2026-07-03 13:19:56.522897+02	2026-07-04 01:19:56.518902+02	0:0:0:0:0:0:0:1	2026-07-03 15:16:59.373145+02	ea613126-8688-47a3-976a-942f318a6f27	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
6779ed83-a779-472c-bd9c-87273c8339a0	2026-07-03 15:17:11.583829+02	2026-07-04 03:17:11.544316+02	0:0:0:0:0:0:0:1	2026-07-03 15:58:42.60009+02	63acba1a-bec7-4e66-b8c4-a4b2f9ee5c2a	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
6c552c02-3ee7-4fca-bccd-ab286acba87c	2026-07-03 16:02:42.008765+02	2026-07-04 04:02:41.972147+02	0:0:0:0:0:0:0:1	2026-07-03 16:09:30.106308+02	70a0b5e5-bde5-4ba9-a8d8-54070728549b	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
c65b032c-4032-4284-9a8a-89791d1cc5b7	2026-07-03 16:09:45.37491+02	2026-07-04 04:09:45.370911+02	0:0:0:0:0:0:0:1	2026-07-03 16:24:43.339697+02	c3a059b4-e1b6-4406-b69e-ebb48780371d	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
86cb3bd3-78ce-4644-98ef-a6c0be2dc1cc	2026-07-03 16:24:51.704696+02	2026-07-04 04:24:51.702684+02	0:0:0:0:0:0:0:1	2026-07-03 16:39:20.583039+02	5a5323ca-4ebe-475d-9695-e847d032688f	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
e359aade-15ec-430c-9855-339f39306763	2026-07-03 16:39:29.73839+02	2026-07-04 04:39:29.737395+02	0:0:0:0:0:0:0:1	2026-07-04 09:16:01.920267+02	c0ad6e16-2ce2-486c-8166-a69e96ba1abc	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
46e27fab-b2cb-44b2-a00a-bbe85b1fa57b	2026-07-04 09:23:49.428951+02	2026-07-04 21:23:49.426951+02	0:0:0:0:0:0:0:1	2026-07-04 09:24:21.601137+02	18865d53-b0df-4cfa-9735-32783fe3b606	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
957fc3c3-240a-4526-bdaa-cc5bdc84f912	2026-07-04 09:16:01.922297+02	2026-07-04 21:16:01.921272+02	0:0:0:0:0:0:0:1	2026-07-04 09:23:49.425957+02	d56648b4-d1a4-4ef4-9adc-81c9d5df37d6	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
3b2dd42c-b88f-4a8e-a0a4-4186eeccc26a	2026-07-09 14:07:50.874596+02	2026-07-10 02:07:50.757596+02	0:0:0:0:0:0:0:1	2026-07-09 14:27:46.635344+02	2cdc8f35-7410-44de-a084-7ffd2c608be9	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
e08e87ba-9868-4052-8ccc-611437240770	2026-07-09 14:27:54.371956+02	2026-07-10 02:27:54.369437+02	0:0:0:0:0:0:0:1	2026-07-09 14:28:23.609771+02	80e33f6c-a016-40ed-99cc-dc8640fe2b1c	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0	4b846d3f-ad1e-4732-aed4-d57a956c49d7
20d61a84-e835-48a5-905a-3553332c604e	2026-07-09 14:28:32.475463+02	2026-07-10 02:28:32.473465+02	0:0:0:0:0:0:0:1	2026-07-09 21:21:21.515914+02	37666b05-5fcf-49f1-8d8f-8c9bfa7b52cf	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
d633d49f-022e-44e1-a50d-1ba515cc6828	2026-07-09 21:21:21.625917+02	2026-07-10 09:21:21.519915+02	0:0:0:0:0:0:0:1	2026-07-10 08:52:28.174195+02	1437e051-f220-42ed-9519-c980d9e04287	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
75684886-41e2-453b-b8bd-67351fef6865	2026-07-10 08:55:34.684797+02	2026-07-10 20:55:34.647795+02	0:0:0:0:0:0:0:1	2026-07-10 09:13:35.878056+02	d05d7cc9-2394-4472-aaa7-acfb6b116c89	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	4b846d3f-ad1e-4732-aed4-d57a956c49d7
14d2f4a8-b55a-4eb6-a129-be4c89b9ba4c	2026-07-10 09:24:11.428941+02	2026-07-10 21:24:11.42693+02	0:0:0:0:0:0:0:1	2026-07-13 16:46:24.391453+02	ee07ab11-6658-4371-8c27-0e03532840e3	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
98b22efb-f611-4a34-8803-e3275251dc86	2026-07-13 16:46:24.420485+02	2026-07-14 04:46:24.394453+02	0:0:0:0:0:0:0:1	2026-07-13 16:46:28.658866+02	a562cd5d-e982-4ae4-bedd-5db61758ee9d	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
db0da283-8d2e-4c96-a03d-25633025b050	2026-07-13 16:52:31.932172+02	2026-07-14 04:52:31.930171+02	0:0:0:0:0:0:0:1	2026-07-13 17:02:00.77709+02	17073fb8-e1bd-40b0-87c2-e9fd90072d42	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
858ffba4-66e1-4cae-8a20-03d542e8ee54	2026-07-13 17:03:46.436825+02	2026-07-14 05:03:46.434826+02	0:0:0:0:0:0:0:1	2026-07-13 17:07:50.168895+02	b2815d59-8d9f-467f-b735-368ac918a42d	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
8ad34388-e9ee-4513-9872-dd97972b796a	2026-07-13 17:12:10.262504+02	2026-07-14 05:12:10.259509+02	0:0:0:0:0:0:0:1	\N	5f99a00d-4177-44c6-98be-6dbafaa7c06a	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, affiliation, auth_type, created_at, email, full_name, institution, job_title, last_login_at, password_hash, phone, reset_token, reset_token_expires_at, status, unza_id, updated_at, department_id, school_id, email_verification_token, locked_until, login_attempts, email_verified, current_session_id) FROM stdin;
4d0c1078-4b0c-4e74-aa10-ec5157366eb4	government	LOCAL	2026-06-25 14:21:52.856397+02	user@test.com	Test User	ministry of education	student	2026-06-25 14:21:52.791393+02	$2a$10$j6M3nwpIBj/C8h0jLXmQGeQ5EO4bZY4MB4mIieqPHEwClPgEL2n1q	+260976911338	\N	\N	active	\N	2026-06-29 14:28:28.512644+02	\N	\N	\N	\N	0	t	\N
4b846d3f-ad1e-4732-aed4-d57a956c49d7	Agricultural Sciences	OAP	2026-06-25 16:03:43.960622+02	kamutumba@unza.zm	WETU KAMUTUMBA	University of Zambia	Bachelor Of Science Human Nutrition	2026-07-10 08:55:34.633797+02	\N	0979089232	\N	\N	active	2022531784	2026-07-10 09:13:35.882585+02	\N	\N	\N	\N	0	t	\N
67dd7cab-0b3f-4223-828a-d0d2e5b175ba	\N	HR	2026-06-25 16:01:53.491973+02	chrishent.mutondo@unza.ac.zm	Chrishent Mutondo Matakala	University of Zambia	Developer	2026-07-13 17:12:10.247485+02	\N	+260961486104	\N	\N	active	012744	2026-07-13 17:12:10.264503+02	\N	\N	\N	\N	0	t	5f99a00d-4177-44c6-98be-6dbafaa7c06a
0e5b2cbb-c6bf-44bd-a299-d49493946804	\N	LOCAL	2026-06-25 04:07:11.532456+02	admin@sanitation-hub.local	System Administrator	\N	\N	2026-07-02 16:24:34.338496+02	$2a$10$zgDWfaqnD8hFhAtrS3w/3.nJuC.HUQuN30k/IE1jj0RkWxqD5QzDy	\N	\N	\N	active	\N	2026-07-02 16:25:18.051959+02	\N	\N	\N	\N	0	t	\N
\.


--
-- Data for Name: views; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.views (id, ip_address, viewed_at, content_item_id, user_id) FROM stdin;
12	0:0:0:0:0:0:0:1	2026-07-03 09:00:25.924485+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
13	0:0:0:0:0:0:0:1	2026-07-03 09:04:38.045254+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
14	0:0:0:0:0:0:0:1	2026-07-03 09:04:44.112224+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
15	0:0:0:0:0:0:0:1	2026-07-03 09:04:44.121786+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
16	0:0:0:0:0:0:0:1	2026-07-03 09:13:45.9667+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
17	0:0:0:0:0:0:0:1	2026-07-03 09:14:02.279396+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
18	0:0:0:0:0:0:0:1	2026-07-03 09:14:02.336066+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
19	0:0:0:0:0:0:0:1	2026-07-03 09:14:25.005597+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
20	0:0:0:0:0:0:0:1	2026-07-03 09:14:32.688713+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
21	0:0:0:0:0:0:0:1	2026-07-03 09:21:31.560002+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
22	0:0:0:0:0:0:0:1	2026-07-03 09:32:32.908988+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
23	0:0:0:0:0:0:0:1	2026-07-03 09:36:13.217391+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
24	0:0:0:0:0:0:0:1	2026-07-03 09:36:25.797811+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
25	0:0:0:0:0:0:0:1	2026-07-03 09:47:09.477044+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
26	0:0:0:0:0:0:0:1	2026-07-03 09:47:26.906045+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
27	0:0:0:0:0:0:0:1	2026-07-03 09:47:31.370986+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
28	0:0:0:0:0:0:0:1	2026-07-03 10:02:46.602273+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
29	0:0:0:0:0:0:0:1	2026-07-03 10:02:54.717902+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
30	0:0:0:0:0:0:0:1	2026-07-03 10:02:54.728446+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
31	0:0:0:0:0:0:0:1	2026-07-03 10:08:25.026561+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
32	0:0:0:0:0:0:0:1	2026-07-03 10:39:12.193604+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
33	0:0:0:0:0:0:0:1	2026-07-03 13:20:39.649308+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	4b846d3f-ad1e-4732-aed4-d57a956c49d7
34	0:0:0:0:0:0:0:1	2026-07-03 13:21:17.464377+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
35	0:0:0:0:0:0:0:1	2026-07-03 13:36:06.790517+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	4b846d3f-ad1e-4732-aed4-d57a956c49d7
36	0:0:0:0:0:0:0:1	2026-07-03 14:01:57.934014+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
37	0:0:0:0:0:0:0:1	2026-07-03 14:07:48.533917+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	4b846d3f-ad1e-4732-aed4-d57a956c49d7
38	0:0:0:0:0:0:0:1	2026-07-03 14:08:01.399555+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	4b846d3f-ad1e-4732-aed4-d57a956c49d7
39	0:0:0:0:0:0:0:1	2026-07-09 14:12:44.071907+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
40	0:0:0:0:0:0:0:1	2026-07-09 21:12:09.418455+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
41	0:0:0:0:0:0:0:1	2026-07-10 09:14:54.148779+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	\N
42	0:0:0:0:0:0:0:1	2026-07-13 16:51:50.020722+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	\N
43	0:0:0:0:0:0:0:1	2026-07-13 17:00:44.063165+02	ad01694d-68dc-4b18-9ab5-37e89f5b5f79	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
44	0:0:0:0:0:0:0:1	2026-07-13 17:01:27.318142+02	10bccde4-31bf-42e6-8799-4d2500c1d14b	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
45	0:0:0:0:0:0:0:1	2026-07-13 17:07:00.076881+02	c3957540-64d6-46c1-8032-4c89dc324923	67dd7cab-0b3f-4223-828a-d0d2e5b175ba
\.


--
-- Name: downloads_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.downloads_id_seq', 3, true);


--
-- Name: views_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.views_id_seq', 45, true);


--
-- Name: approval_workflows approval_workflows_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_workflows
    ADD CONSTRAINT approval_workflows_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: bookmarks bookmarks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bookmarks
    ADD CONSTRAINT bookmarks_pkey PRIMARY KEY (id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: content_files content_files_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_files
    ADD CONSTRAINT content_files_pkey PRIMARY KEY (id);


--
-- Name: content_items content_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_items
    ADD CONSTRAINT content_items_pkey PRIMARY KEY (id);


--
-- Name: content_tags content_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_tags
    ADD CONSTRAINT content_tags_pkey PRIMARY KEY (content_item_id, tag_id);


--
-- Name: content_type_defs content_type_defs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_type_defs
    ADD CONSTRAINT content_type_defs_pkey PRIMARY KEY (id);


--
-- Name: demonstration_sites demonstration_sites_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.demonstration_sites
    ADD CONSTRAINT demonstration_sites_pkey PRIMARY KEY (id);


--
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (id);


--
-- Name: digitization_records digitization_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitization_records
    ADD CONSTRAINT digitization_records_pkey PRIMARY KEY (id);


--
-- Name: downloads downloads_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.downloads
    ADD CONSTRAINT downloads_pkey PRIMARY KEY (id);


--
-- Name: events events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: hero_banners hero_banners_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hero_banners
    ADD CONSTRAINT hero_banners_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: reading_list_items reading_list_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reading_list_items
    ADD CONSTRAINT reading_list_items_pkey PRIMARY KEY (id);


--
-- Name: reading_lists reading_lists_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reading_lists
    ADD CONSTRAINT reading_lists_pkey PRIMARY KEY (id);


--
-- Name: schools schools_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.schools
    ADD CONSTRAINT schools_pkey PRIMARY KEY (id);


--
-- Name: system_settings system_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT system_settings_pkey PRIMARY KEY (id);


--
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: training_registrations training_registrations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_registrations
    ADD CONSTRAINT training_registrations_pkey PRIMARY KEY (id);


--
-- Name: training_sessions training_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_sessions
    ADD CONSTRAINT training_sessions_pkey PRIMARY KEY (id);


--
-- Name: events uk153gnccmeyks5dlt154kkgch5; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT uk153gnccmeyks5dlt154kkgch5 UNIQUE (slug);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: training_registrations ukcbandm4q2gh7irix5xbwa3nve; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_registrations
    ADD CONSTRAINT ukcbandm4q2gh7irix5xbwa3nve UNIQUE (session_id, user_id);


--
-- Name: demonstration_sites ukedm8m5iye0d4lqva1db5xn1fp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.demonstration_sites
    ADD CONSTRAINT ukedm8m5iye0d4lqva1db5xn1fp UNIQUE (slug);


--
-- Name: schools ukehwqlfa7xseucba45p6wlqfgn; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.schools
    ADD CONSTRAINT ukehwqlfa7xseucba45p6wlqfgn UNIQUE (name);


--
-- Name: content_items ukgqkchofk6igo2uk78bhdmxc9l; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_items
    ADD CONSTRAINT ukgqkchofk6igo2uk78bhdmxc9l UNIQUE (slug);


--
-- Name: training_sessions ukhbgoyk237st6rcyooc23b0oe5; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_sessions
    ADD CONSTRAINT ukhbgoyk237st6rcyooc23b0oe5 UNIQUE (slug);


--
-- Name: content_type_defs ukkk4n7kwdk9n9wevu54cct399; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_type_defs
    ADD CONSTRAINT ukkk4n7kwdk9n9wevu54cct399 UNIQUE (name);


--
-- Name: users ukn58nl83eiuyspdek47ywaq7e6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukn58nl83eiuyspdek47ywaq7e6 UNIQUE (unza_id);


--
-- Name: system_settings uknm18l4pyovtvd8y3b3x0l2y64; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT uknm18l4pyovtvd8y3b3x0l2y64 UNIQUE (setting_key);


--
-- Name: categories ukoul14ho7bctbefv8jywp5v3i2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT ukoul14ho7bctbefv8jywp5v3i2 UNIQUE (slug);


--
-- Name: content_type_defs ukoxlva2eohsxthv86j27sqh2uc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_type_defs
    ADD CONSTRAINT ukoxlva2eohsxthv86j27sqh2uc UNIQUE (slug);


--
-- Name: tags uksn0d91hxu700qcw0n4pebp5vc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT uksn0d91hxu700qcw0n4pebp5vc UNIQUE (slug);


--
-- Name: tags ukt48xdq560gs3gap9g7jg36kgc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT ukt48xdq560gs3gap9g7jg36kgc UNIQUE (name);


--
-- Name: categories ukt8o6pivur7nn124jehx7cygw5; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT ukt8o6pivur7nn124jehx7cygw5 UNIQUE (name);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);


--
-- Name: user_sessions user_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: views views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.views
    ADD CONSTRAINT views_pkey PRIMARY KEY (id);


--
-- Name: idx_banner_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_banner_order ON public.hero_banners USING btree (display_order);


--
-- Name: idx_ci_access_level; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_access_level ON public.content_items USING btree (access_level);


--
-- Name: idx_ci_author_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_author_id ON public.content_items USING btree (author_id);


--
-- Name: idx_ci_category_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_category_id ON public.content_items USING btree (category_id);


--
-- Name: idx_ci_content_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_content_type ON public.content_items USING btree (content_type);


--
-- Name: idx_ci_deleted_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_deleted_at ON public.content_items USING btree (deleted_at);


--
-- Name: idx_ci_department_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_department_id ON public.content_items USING btree (department_id);


--
-- Name: idx_ci_published_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_published_at ON public.content_items USING btree (published_at);


--
-- Name: idx_ci_search; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_search ON public.content_items USING gin (search_vector);


--
-- Name: idx_ci_slug; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_slug ON public.content_items USING btree (slug);


--
-- Name: idx_ci_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_status ON public.content_items USING btree (status);


--
-- Name: idx_ci_status_published; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ci_status_published ON public.content_items USING btree (status, published_at);


--
-- Name: idx_ds_site_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ds_site_type ON public.demonstration_sites USING btree (site_type);


--
-- Name: idx_ds_slug; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ds_slug ON public.demonstration_sites USING btree (slug);


--
-- Name: idx_ds_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ds_status ON public.demonstration_sites USING btree (operational_status);


--
-- Name: idx_us_session_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_us_session_id ON public.user_sessions USING btree (session_id);


--
-- Name: idx_us_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_us_user_id ON public.user_sessions USING btree (user_id);


--
-- Name: idx_users_auth_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_auth_type ON public.users USING btree (auth_type);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_status ON public.users USING btree (status);


--
-- Name: idx_users_unza_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_unza_id ON public.users USING btree (unza_id);


--
-- Name: content_items content_search_vector_update; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER content_search_vector_update BEFORE INSERT OR UPDATE ON public.content_items FOR EACH ROW EXECUTE FUNCTION public.update_content_search_vector();


--
-- Name: reading_list_items fk1crx5mu50ssqj1eminyihjfwa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reading_list_items
    ADD CONSTRAINT fk1crx5mu50ssqj1eminyihjfwa FOREIGN KEY (list_id) REFERENCES public.reading_lists(id);


--
-- Name: content_files fk1w7q8ebm64efudwl4sd6bhoit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_files
    ADD CONSTRAINT fk1w7q8ebm64efudwl4sd6bhoit FOREIGN KEY (uploaded_by) REFERENCES public.users(id);


--
-- Name: events fk2oib4sxiq6yfydyf20mopjqh4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT fk2oib4sxiq6yfydyf20mopjqh4 FOREIGN KEY (department_id) REFERENCES public.departments(id);


--
-- Name: users fk3gj5j7vnsoxf1wp9n5hsqdiq3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk3gj5j7vnsoxf1wp9n5hsqdiq3 FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: approval_workflows fk5bf48cf2trdm13i2sybkfi7l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_workflows
    ADD CONSTRAINT fk5bf48cf2trdm13i2sybkfi7l FOREIGN KEY (content_item_id) REFERENCES public.content_items(id);


--
-- Name: approval_workflows fk6vn9tl2lnd0a0gqap5o43rbbc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_workflows
    ADD CONSTRAINT fk6vn9tl2lnd0a0gqap5o43rbbc FOREIGN KEY (reviewed_by) REFERENCES public.users(id);


--
-- Name: downloads fk81khr7pd7uawx3jre5h1ju1di; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.downloads
    ADD CONSTRAINT fk81khr7pd7uawx3jre5h1ju1di FOREIGN KEY (content_file_id) REFERENCES public.content_files(id);


--
-- Name: user_sessions fk8klxsgb8dcjjklmqebqp1twd5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT fk8klxsgb8dcjjklmqebqp1twd5 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: approval_workflows fk92q54qn9cnwcps7wihvyyprqx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_workflows
    ADD CONSTRAINT fk92q54qn9cnwcps7wihvyyprqx FOREIGN KEY (published_by) REFERENCES public.users(id);


--
-- Name: departments fk9fwvupr4xfhftlrl40k0ga8u5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT fk9fwvupr4xfhftlrl40k0ga8u5 FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: notifications fk9y21adhxn0ayjhfocscqox7bh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reading_lists fka37xwirr81olj9dto5fp30m8m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reading_lists
    ADD CONSTRAINT fka37xwirr81olj9dto5fp30m8m FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: digitization_records fkac5wckuv2esafxb7wtuvibqdf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitization_records
    ADD CONSTRAINT fkac5wckuv2esafxb7wtuvibqdf FOREIGN KEY (reviewed_by) REFERENCES public.users(id);


--
-- Name: content_items fkb07xn2a656eek53jtkllb35la; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_items
    ADD CONSTRAINT fkb07xn2a656eek53jtkllb35la FOREIGN KEY (author_id) REFERENCES public.users(id);


--
-- Name: training_registrations fkb5ak7tqe9ocd33wxei56xphy6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_registrations
    ADD CONSTRAINT fkb5ak7tqe9ocd33wxei56xphy6 FOREIGN KEY (session_id) REFERENCES public.training_sessions(id);


--
-- Name: digitization_records fkc7j8fp6hautxql9odjfrlnu8j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitization_records
    ADD CONSTRAINT fkc7j8fp6hautxql9odjfrlnu8j FOREIGN KEY (content_item_id) REFERENCES public.content_items(id);


--
-- Name: content_tags fkcr5rprg24ccjyjsnc7cac859f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_tags
    ADD CONSTRAINT fkcr5rprg24ccjyjsnc7cac859f FOREIGN KEY (tag_id) REFERENCES public.tags(id);


--
-- Name: system_settings fkda4dsfh0rkqhmq0bo7plxiqm3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT fkda4dsfh0rkqhmq0bo7plxiqm3 FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: bookmarks fkdbsho2e05w5r13fkjqfjmge5f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bookmarks
    ADD CONSTRAINT fkdbsho2e05w5r13fkjqfjmge5f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: content_items fke768af23n3umi1xbma0l0epdb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_items
    ADD CONSTRAINT fke768af23n3umi1xbma0l0epdb FOREIGN KEY (department_id) REFERENCES public.departments(id);


--
-- Name: digitization_records fkeqcik8vcbyxxi6g1to7x6kabo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitization_records
    ADD CONSTRAINT fkeqcik8vcbyxxi6g1to7x6kabo FOREIGN KEY (scanned_by) REFERENCES public.users(id);


--
-- Name: content_items fkesqkldy6fdhx5do3tlbisd4wu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_items
    ADD CONSTRAINT fkesqkldy6fdhx5do3tlbisd4wu FOREIGN KEY (category_id) REFERENCES public.categories(id);


--
-- Name: views fkfpjhiw62pqb70q2j46jp3h4vk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.views
    ADD CONSTRAINT fkfpjhiw62pqb70q2j46jp3h4vk FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: training_sessions fkgiori1ym24n4t1lfm5f6rcdjl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_sessions
    ADD CONSTRAINT fkgiori1ym24n4t1lfm5f6rcdjl FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reading_list_items fki0bmh0p2ye7656ryufnfdyhin; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reading_list_items
    ADD CONSTRAINT fki0bmh0p2ye7656ryufnfdyhin FOREIGN KEY (content_id) REFERENCES public.content_items(id);


--
-- Name: bookmarks fkiytm6j9l61isatycr6jfjh5fu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bookmarks
    ADD CONSTRAINT fkiytm6j9l61isatycr6jfjh5fu FOREIGN KEY (content_id) REFERENCES public.content_items(id);


--
-- Name: downloads fkjb9vjhmpns24p7vuyjcc0wa5l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.downloads
    ADD CONSTRAINT fkjb9vjhmpns24p7vuyjcc0wa5l FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: training_registrations fkjno8ggrohf6k4oera70ld6529; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.training_registrations
    ADD CONSTRAINT fkjno8ggrohf6k4oera70ld6529 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: audit_logs fkjs4iimve3y0xssbtve5ysyef0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT fkjs4iimve3y0xssbtve5ysyef0 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: events fkmpv90a1lsx9lcxsj7xjcvvsxg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT fkmpv90a1lsx9lcxsj7xjcvvsxg FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: content_tags fknvkbh0i148315l6j7jrsq1jyw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_tags
    ADD CONSTRAINT fknvkbh0i148315l6j7jrsq1jyw FOREIGN KEY (content_item_id) REFERENCES public.content_items(id);


--
-- Name: views fko79kj8lqpao94anyffcaj9u39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.views
    ADD CONSTRAINT fko79kj8lqpao94anyffcaj9u39 FOREIGN KEY (content_item_id) REFERENCES public.content_items(id);


--
-- Name: approval_workflows fkpqx8wehpkmrvxj8qsanruawf6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_workflows
    ADD CONSTRAINT fkpqx8wehpkmrvxj8qsanruawf6 FOREIGN KEY (submitted_by) REFERENCES public.users(id);


--
-- Name: content_files fkro1sdc19p37f5otg0wcjiko0h; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_files
    ADD CONSTRAINT fkro1sdc19p37f5otg0wcjiko0h FOREIGN KEY (content_item_id) REFERENCES public.content_items(id);


--
-- Name: categories fksaok720gsu4u2wrgbk10b5n8d; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT fksaok720gsu4u2wrgbk10b5n8d FOREIGN KEY (parent_id) REFERENCES public.categories(id);


--
-- Name: users fksbg59w8q63i0oo53rlgvlcnjq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fksbg59w8q63i0oo53rlgvlcnjq FOREIGN KEY (department_id) REFERENCES public.departments(id);


--
-- PostgreSQL database dump complete
--

\unrestrict roiZCsDBjQHccn8j6sigM45sZDj9eT4v2gNZb3A5bSO2N5Lqk7YRT8bhynFowgP

