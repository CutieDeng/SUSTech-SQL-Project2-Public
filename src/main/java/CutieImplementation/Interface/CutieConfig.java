package CutieImplementation.Interface;

public class CutieConfig {
    private boolean doubleQuotationPreferred = true;

    private static CutieConfig instance = new CutieConfig();

    public static CutieConfig getInstance() {
        return instance;
    }

    public boolean isDoubleQuotationPreferred() {
        return doubleQuotationPreferred;
    }
}
