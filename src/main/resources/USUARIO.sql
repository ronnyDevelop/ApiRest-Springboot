CREATE TABLE Usuario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL,
    token VARCHAR(255),
    creado TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado TIMESTAMP NOT NULL DEFAULT NOW(),
    ultimoLogin TIMESTAMP
);
