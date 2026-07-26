package com.ferronor.sic.conexion;

public final class TransactionContext implements AutoCloseable {

    private final boolean esDuenio;
    private boolean finalizada = false;

    TransactionContext(boolean esDuenio) {
        this.esDuenio = esDuenio;
    }

    public void commit() {
        TransactionManager.confirmar(esDuenio);
        finalizada = true;
    }

    public void rollback() {
        TransactionManager.revertir(esDuenio);
        finalizada = true;
    }

    @Override
    public void close() {
        if (!finalizada) {

            System.err.println(
                    "Advertencia: transacción cerrada sin commit(), aplicando rollback.");

            TransactionManager.revertir(esDuenio);
        }
    }
}
