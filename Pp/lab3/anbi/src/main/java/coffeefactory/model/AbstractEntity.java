package coffeefactory.model;

import java.util.Map;

/**
 * Абстрактная сущность каталога. Общие данные и контракт сериализации в строку файла.
 */
public abstract class AbstractEntity {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public abstract String toFileLine();

    public abstract Map<String, String> toFieldMap();
}
