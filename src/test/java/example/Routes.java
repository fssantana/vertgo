package example;

import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.VertgoHandler;
import java.util.Arrays;
import java.util.List;

public class Routes extends VertgoHandler {

    @Override
    protected List<Controller> router() {
        return Arrays.asList(
                new ExampleController()
        );
    }

}
