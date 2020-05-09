import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.Template;
import com.github.coreyshupe.foi.template.internal.StringTemplate;
import com.github.coreyshupe.foi.template.internal.primitive.IntTemplate;
import com.github.coreyshupe.foi.template.internal.primitive.LongTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ExampleObjectTemplate extends Template<ExampleObject> {
    private final StringTemplate stringTemplate = StringTemplate.getInstance();
    private final IntTemplate intTemplate = IntTemplate.getInstance();
    private final LongTemplate longTemplate = LongTemplate.getInstance();

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return ExampleObject.class.isAssignableFrom(givenType);
    }

    @Override public int sizeOf(@NotNull ExampleObject object) {
        return stringTemplate.sizeOf(object.getFirstString()) +
                stringTemplate.sizeOf(object.getSecondString()) +
                stringTemplate.sizeOf(object.getThirdString()) +
                intTemplate.sizeOf(object.getObjectInt()) +
                longTemplate.sizeOf(object.getObjectLong());
    }

    @Override
    public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull ExampleObject object, @NotNull ByteBuffer buffer) {
        stringTemplate.writeToBuffer(linker, object.getFirstString(), buffer);
        stringTemplate.writeToBuffer(linker, object.getSecondString(), buffer);
        stringTemplate.writeToBuffer(linker, object.getThirdString(), buffer);
        intTemplate.writeToBuffer(linker, object.getObjectInt(), buffer);
        longTemplate.writeToBuffer(linker, object.getObjectLong(), buffer);
    }

    @NotNull @Override
    public ExampleObject readFromWalker(@NotNull TemplateLinker linker, @NotNull TemplateWalker walker) throws IOException {
        return ExampleObject.builder()
                .firstString(stringTemplate.readFromWalker(linker, walker))
                .secondString(stringTemplate.readFromWalker(linker, walker))
                .thirdString(stringTemplate.readFromWalker(linker, walker))
                .objectInt(intTemplate.readFromWalker(linker, walker))
                .objectLong(longTemplate.readFromWalker(linker, walker))
                .build();
    }
}
