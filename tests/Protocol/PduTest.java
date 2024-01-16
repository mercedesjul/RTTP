package Protocol;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class PduTest {

  @Test
   void intToByteArray() {

  }

  @Test
  void byteArrayToInt() {
  }

  @Test
  void sendSuperHeader() {
  }

  @Test
  void send() {
  }

  @Test
  void stream() {
  }

  @Test
  void addStringElement() {
  }

  @Test
  void longToInts() {
    ArrayList<Integer> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add(Integer.MAX_VALUE);
    }
    assertIterableEquals(list, Pdu.longToIntsChunks(Integer.MAX_VALUE * 100L));
  }

  @Test
  void getExplodedLastElement() {
  }

  @Test
  void createPduFromInputStream() {
  }
}