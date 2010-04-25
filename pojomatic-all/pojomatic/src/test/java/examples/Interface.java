package examples;

import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.Pojomator;
import org.pojomatic.Pojomatic;

@AutoProperty
public interface Interface {
  static Pojomator<Interface> POJOMATOR = Pojomatic.pojomator(Interface.class);

  String getName();
}
