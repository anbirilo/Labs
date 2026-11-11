package coffeefactory.model;

import java.util.Date;
import java.util.Locale;

import coffeefactory.util.DateUtil;

public class CoffeeMaker extends Appliance {
    private CoffeeMakerType type;
    private int powerW;
    private int tankVolumeMl;

    public CoffeeMaker(int id, CoffeeMakerType type, String model, int powerW,
                       int tankVolumeMl, Date releaseDate, double price) {
        super(id, model, releaseDate, price);
        this.type = type;
        this.powerW = powerW;
        this.tankVolumeMl = tankVolumeMl;
    }

    public CoffeeMaker(CoffeeMaker other) {
        this(other.getId(), other.type, other.getModel(), other.powerW,
                other.tankVolumeMl, other.getReleaseDate(), other.getPrice());
    }

    @Override
    public String getCategory() {
        return "Кофеварка";
    }

    public CoffeeMakerType getType() {
        return type;
    }

    public void setType(CoffeeMakerType type) {
        this.type = type;
    }

    public int getPowerW() {
        return powerW;
    }

    public void setPowerW(int powerW) {
        this.powerW = powerW;
    }

    public int getTankVolumeMl() {
        return tankVolumeMl;
    }

    public void setTankVolumeMl(int tankVolumeMl) {
        this.tankVolumeMl = tankVolumeMl;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "%s [id=%d, тип=%s, модель=%s, мощность=%d Вт, бак=%d мл, выпуск=%s, цена=%.2f]",
                getCategory(), getId(), type, getModel(), powerW, tankVolumeMl,
                DateUtil.format(getReleaseDate()), getPrice());
    }
}
