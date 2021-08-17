package jsonapi.internal.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import jsonapi.Id;
import jsonapi.JsonApiFactory;
import jsonapi.Lid;
import jsonapi.LinksObject;
import jsonapi.MetaObject;
import jsonapi.RelationshipsObject;
import jsonapi.ToMany;
import jsonapi.ToOne;
import jsonapi.Type;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransientAdapterTestJava {

  // Class that will be handled by java reflection adapter (ClassJsonAdapter)
  static class ReflectClass {
    public Object nonTransient;
    @Type public Object transient1;
    @Id public Object transient2;
    @Lid public Object transient3;
    @RelationshipsObject public Object transient4;
    @LinksObject public Object transient5;
    @MetaObject public Object transient6;
    @ToOne(name = "ONE") public Object transient7;
    @ToMany(name = "MANY") public List<Object> transient8;
  }

  private final Moshi moshi = new Moshi.Builder()
    .add(new JsonApiFactory.Builder().build())
    .build();

  private final JsonAdapter<ReflectClass> adapter = moshi.adapter(ReflectClass.class);

  @Test
  public void deserialize() throws IOException {
    String json = "{" +
      "\"nonTransient\":\"value\"," +
      "\"transient1\":\"value\"," +
      "\"transient2\":\"value\"," +
      "\"transient3\":\"value\"," +
      "\"transient4\":\"value\"," +
      "\"transient5\":\"value\"," +
      "\"transient6\":\"value\"," +
      "\"transient7\":\"value\"," +
      "\"transient8\":[\"value\"]" +
      "}";

    assertThat(adapter.fromJson(json))
      .hasFieldOrPropertyWithValue("nonTransient", "value")
      .hasAllNullFieldsOrPropertiesExcept("nonTransient");
  }

  @Test
  public void serialize() {
    ReflectClass value = new ReflectClass();
    value.nonTransient = "value";
    value.transient1 = "value";
    value.transient2 = "value";
    value.transient3 = "value";
    value.transient4 = "value";
    value.transient5 = "value";
    value.transient6 = "value";
    value.transient7 = "value";
    value.transient8 = Arrays.asList("value", "value");

    String serialized = adapter.toJson(value);

    assertThat(serialized).isEqualTo("{\"nonTransient\":\"value\"}");
  }
}
