module org.jamon.moduletest{
  requires java.base;
  requires org.pojomatic;
  exports org.pojomatic.moduletest;
  opens org.pojomatic.moduletest to org.pojomatic;
}
