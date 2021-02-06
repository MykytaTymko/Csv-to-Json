package com.mycompany.test;

/**
 *
 * @author Mykyta Tymko
 */
public class Data {

    private String date;
    private Integer id;
    private double value;
    private Integer validFlag;

    public Data(String date, Integer id, double value, Integer validFlag) {
        this.date = date;
        this.id = id;
        this.value = value;
        this.validFlag = validFlag;
    }

    public Data() {
    }

    @Override
    public String toString() {
        return "Data{" + "date=" + date + ", id=" + id + ", value=" + value + ", validFlag=" + validFlag + '}';
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Integer getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(Integer validFlag) {
        this.validFlag = validFlag;
    }

}
