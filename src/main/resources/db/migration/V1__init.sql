CREATE TYPE user_role AS ENUM ('ESTUDIANTE', 'PROFESOR');
CREATE TYPE tipo_analisis AS ENUM ('THEVENIN', 'NORTON', 'AMBOS');

CREATE TABLE users (
    id                      BIGSERIAL PRIMARY KEY,
    username                TEXT UNIQUE NOT NULL,
    password_hash           TEXT NOT NULL,
    role                    user_role NOT NULL,
    nombre                  TEXT,
    apellido1               TEXT,
    apellido2               TEXT,
    pregunta_seguridad      TEXT,
    respuesta_seguridad     TEXT,
    calificacion_general    REAL DEFAULT 0,
    comentarios_profesor    TEXT,
    fecha_creacion          TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE teoria (
    id                      BIGSERIAL PRIMARY KEY,
    titulo                  TEXT NOT NULL,
    contenido               TEXT NOT NULL,
    indice_orden            INTEGER DEFAULT 0,
    creado_por              BIGINT REFERENCES users(id),
    fecha_creacion          TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE ejercicios (
    id                      BIGSERIAL PRIMARY KEY,
    titulo                  TEXT NOT NULL,
    descripcion             TEXT NOT NULL,
    datos_circuito          JSONB NOT NULL,
    solucion_vth            REAL,
    solucion_rth            REAL,
    solucion_in             REAL,
    solucion_rn             REAL,
    tipo_analisis           tipo_analisis NOT NULL,
    dificultad              INTEGER CHECK (dificultad BETWEEN 1 AND 5) DEFAULT 1,
    creado_por              BIGINT REFERENCES users(id),
    nodo_a                  INTEGER DEFAULT -1,
    nodo_b                  INTEGER DEFAULT -1,
    fecha_creacion          TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE documentos_pdf (
    id                      BIGSERIAL PRIMARY KEY,
    titulo                  TEXT NOT NULL,
    descripcion             TEXT NOT NULL,
    nombre_archivo          TEXT NOT NULL,
    path_archivo            TEXT NOT NULL,
    tamano_archivo          INTEGER DEFAULT 0,
    subido_por              BIGINT REFERENCES users(id),
    fecha_subida            TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE progreso_estudiante (
    id                      BIGSERIAL PRIMARY KEY,
    estudiante_id           BIGINT NOT NULL REFERENCES users(id),
    ejercicio_id            BIGINT NOT NULL REFERENCES ejercicios(id),
    completado_fecha        TIMESTAMPTZ DEFAULT now(),
    puntuacion              REAL DEFAULT 0,
    intentos                INTEGER DEFAULT 1,
    tiempo                  INTEGER DEFAULT 0,
    UNIQUE (estudiante_id, ejercicio_id)
);

CREATE TABLE progreso_teoria (
    user_id                 BIGINT REFERENCES users(id),
    teoria_id               BIGINT REFERENCES teoria(id),
    fecha_lectura           TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (user_id, teoria_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY(teoria_id) REFERENCES teoria(id)
);