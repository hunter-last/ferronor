CREATE TABLE auditoria (
    id_auditoria SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha_hora TIMESTAMP NOT NULL DEFAULT now(),
    tabla_afectada VARCHAR(50) NOT NULL,
    id_registro_afectado INT NOT NULL,
    operacion VARCHAR(20) NOT NULL CHECK (operacion IN ('INSERT','UPDATE','DELETE','LOGIN','LOGOUT','GENERAR_REPORTE')),
    descripcion TEXT,
    nombre_equipo VARCHAR(100)
);