CREATE TABLE Telefono (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero VARCHAR(20) NOT NULL,
    codigoCiudad VARCHAR(10),
    codigoPais VARCHAR(5),
    user_id UUID,
    CONSTRAINT fk_usuario FOREIGN KEY (user_id) REFERENCES Usuario(id) ON DELETE CASCADE
);
