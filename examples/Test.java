import com.github.coreyshupe.foi.ChannelObjectInjector;
import com.github.coreyshupe.foi.FileObjectInjector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Test {
    public static void main(String[] args) {
        File f = new File("test.txt");

        ExampleObject object = ExampleObject.builder()
                .firstString("First String Test")
                .secondString("Second String Test")
                .thirdString("Some Third String")
                .objectInt(1234)
                .objectLong(923423)
                .build();

        UUID id = UUID.randomUUID();
        System.out.println("Randomized UUID: " + id);
        System.out.println();
        List<String> stringList = Arrays.asList("abc", "123", "78123", "Testing");

        ChannelObjectInjector objectInjector = new ChannelObjectInjector();
        objectInjector.getTemplateLinker().addTemplate(new ExampleObjectTemplate());
        try (FileOutputStream stream = new FileOutputStream(f)) {
            try (FileObjectInjector injector = new FileObjectInjector(objectInjector, stream.getChannel())) {

                injector.writeInt(123);
                injector.write("Test String");
                injector.writeChar('c');
                injector.writeLong(100_321_123L);
                injector.write(object);
                injector.writeStringCollection(stringList);
                injector.write(id);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileInputStream stream = new FileInputStream(f)) {
            try (FileObjectInjector injector = new FileObjectInjector(objectInjector, stream.getChannel())) {

                System.out.println(injector.readInt());
                System.out.println(injector.readString());
                System.out.println(injector.readChar());
                System.out.println(injector.readLong());
                System.out.println(injector.read(ExampleObject.class).orElse(null));
                System.out.println(Arrays.toString(injector.readStringCollection().toArray(new String[]{})));
                System.out.println(injector.readUUID());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
