package coffeefactory.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
public class CoffeeMachine extends AbstractEntity {
    public static final String DATE_PATTERN = "dd.MM.yyyy";
    public static final String[] COLUMNS = {
            "id",
            "type",
            "model",
            "powerWatts",
            "pressureBar",
            "waterTankMl",
            "cupsPerCycle",
            "manufactureDate",
            "price",
            "manufacturer",
            "color",
            "warrantyMonths"
    };

    private MachineType type;
    private String model = "";
    private int powerWatts;
    private double pressureBar;
    private int waterTankMl;
    private int cupsPerCycle;
    private Date manufactureDate;
    private double price;
    private String manufacturer = "";
    private String color = "";
    private int warrantyMonths;

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

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        format.setLenient(false);
        return format.format(date);
    }

    public static String formatDecimal(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public Map<String, String> toFieldMap() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("id", getId() <= 0 ? "" : Long.toString(getId()));
        map.put("type", type == null ? "" : type.name());
        map.put("model", model == null ? "" : model);
        map.put("powerWatts", Integer.toString(powerWatts));
        map.put("pressureBar", formatDecimal(pressureBar));
        map.put("waterTankMl", Integer.toString(waterTankMl));
        map.put("cupsPerCycle", Integer.toString(cupsPerCycle));
        map.put("manufactureDate", formatDate(manufactureDate));
        map.put("price", formatDecimal(price));
        map.put("manufacturer", manufacturer == null ? "" : manufacturer);
        map.put("color", color == null ? "" : color);
        map.put("warrantyMonths", Integer.toString(warrantyMonths));
        return map;
    }

    @Override
    public String toFileLine() {
        Map<String, String> map = toFieldMap();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < COLUMNS.length; i++) {
            if (i > 0) {
                builder.append(';');
            }
            builder.append(map.get(COLUMNS[i]));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "CoffeeMachine{id=" + getId()
                + ", type=" + (type == null ? "" : type.getTitle() + " (" + type.name() + ")")
                + ", model='" + model + '\''
                + ", powerWatts=" + powerWatts
                + ", pressureBar=" + formatDecimal(pressureBar)
                + ", waterTankMl=" + waterTankMl
                + ", cupsPerCycle=" + cupsPerCycle
                + ", manufactureDate=" + formatDate(manufactureDate)
                + ", price=" + formatDecimal(price)
                + ", manufacturer='" + manufacturer + '\''
                + ", color='" + color + '\''
                + ", warrantyMonths=" + warrantyMonths
                + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CoffeeMachine other = (CoffeeMachine) obj;
        return getId() == other.getId()
                && powerWatts == other.powerWatts
                && waterTankMl == other.waterTankMl
                && cupsPerCycle == other.cupsPerCycle
                && warrantyMonths == other.warrantyMonths
                && Double.compare(pressureBar, other.pressureBar) == 0
                && Double.compare(price, other.price) == 0
                && type == other.type
                && Objects.equals(model, other.model)
                && Objects.equals(manufactureDate, other.manufactureDate)
                && Objects.equals(manufacturer, other.manufacturer)
                && Objects.equals(color, other.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), type, model, powerWatts, pressureBar, waterTankMl,
                cupsPerCycle, manufactureDate, price, manufacturer, color, warrantyMonths);
    }
}
