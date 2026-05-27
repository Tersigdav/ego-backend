package com.ego.backend.database;

public class ParametroQuery<T> {

    private String nome;
    private String operatore;
    private T valore;

    public ParametroQuery(String nome, String operatore, T valore) {
        this.nome = nome;
        this.operatore = operatore;
        this.valore = valore;
    }

    public String getNome() {
        return nome;
    }

    public T getValore() {
        return valore;
    }

    public String getOperatore() {
        return operatore;
    }

    public String toString(boolean conValore) {
        if(conValore)
            return nome+" "+operatore+" "+valore;
        else
            return nome+" "+operatore+" ?";
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

}