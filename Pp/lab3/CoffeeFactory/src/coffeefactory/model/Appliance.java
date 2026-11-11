package coffeefactory.model;

import java.util.Date;


public abstract class Appliance {
    private int id;
    private String model;
    private Date releaseDate;
    private double price;

    protected Appliance(int id, String model, Date releaseDate, double price) {
        this.id = id;
        this.model = model;
        this.releaseDate = releaseDate;
        this.price = price;
    }

    public abstract String getCategory();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
