package coffeefactory.assembly;

import coffeefactory.model.MachineType;

import java.util.Date;

/**
 * Заявка на сборку кофеварки.
 */
public class AssemblyRequest {
    private long id;
    private MachineType type;
    private String model;
    private int powerWatts;
    private double pressureBar;
    private int waterTankMl;
    private int cupsPerCycle;
    private Date manufactureDate;
    private double price;
    private String manufacturer = "";
    private String color = "";
    private int warrantyMonths;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MachineType getType() {
        return type;
    }

    public void setType(MachineType type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPowerWatts() {
        return powerWatts;
    }

    public void setPowerWatts(int powerWatts) {
        this.powerWatts = powerWatts;
    }

    public double getPressureBar() {
        return pressureBar;
    }

    public void setPressureBar(double pressureBar) {
        this.pressureBar = pressureBar;
    }

    public int getWaterTankMl() {
        return waterTankMl;
    }

    public void setWaterTankMl(int waterTankMl) {
        this.waterTankMl = waterTankMl;
    }

    public int getCupsPerCycle() {
        return cupsPerCycle;
    }

    public void setCupsPerCycle(int cupsPerCycle) {
        this.cupsPerCycle = cupsPerCycle;
    }

    public Date getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(Date manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }
}
