package fr.cnrs.iremus.sherlock.common;

public class Triple {
    private String s;
    private String p;
    private String o;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public String toString() {
        return this.getS() + " " + this.getP() + " " + this.getO();
    }
}
