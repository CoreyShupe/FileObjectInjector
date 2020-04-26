import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExampleObject {
    private final String firstString;
    private final String secondString;
    private final int objectInt;
    private final long objectLong;
    private final String thirdString;

    public ExampleObject(String firstString, String secondString, int objectInt, long objectLong, String thirdString) {
        this.firstString = firstString;
        this.secondString = secondString;
        this.objectInt = objectInt;
        this.objectLong = objectLong;
        this.thirdString = thirdString;
    }

    @Override public String toString() {
        return "ExampleObject{" +
                "firstString='" + firstString + '\'' +
                ", secondString='" + secondString + '\'' +
                ", objectInt=" + objectInt +
                ", objectLong=" + objectLong +
                ", thirdString='" + thirdString + '\'' +
                '}';
    }
}
