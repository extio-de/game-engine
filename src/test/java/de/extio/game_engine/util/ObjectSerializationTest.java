package de.extio.game_engine.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class ObjectSerializationTest {
	
	@Test
	public void testSerializeDeserializeBasic() {
		var serialization = new ObjectSerialization();
		var original = new TestObject("Hello", 42, 3.14, List.of("tag1", "tag2"), List.of(new TestObject2("key", true), new TestObject2("key2", false)), Map.of("attr", 1, "attr2", 2));
		
		var serialized = serialization.serialize(original, false, false, false, null, digest -> {
		});
		
		System.out.println("Serialized length: " + serialized.length);
		System.out.println("" + new String(serialized));
		assertNotNull(serialized);
		assertTrue(serialized.length > 0);
		
		var deserialized = serialization.deserialize(TestObject.class, serialized, false, false, false, null, null, result -> {
		});
		
		assertNotNull(deserialized);
		assertEquals(original.name, deserialized.name);
		assertEquals(original.id, deserialized.id);
		assertEquals(original.value, deserialized.value);
	}
	
	@Test
	public void testSerializeDeserializeWithCompression() {
		var serialization = new ObjectSerialization();
		var original = new TestObject("Compressed", 100, 2.71, List.of("a", "b", "c"), List.of(), Map.of());
		
		var serialized = serialization.serialize(original, true, false, false, null, digest -> {
		});
		
		assertNotNull(serialized);
		assertTrue(serialized.length > 0);
		
		var deserialized = serialization.deserialize(TestObject.class, serialized, false, true, false, null, null, result -> {
		});
		
		assertNotNull(deserialized);
		assertEquals(original.name, deserialized.name);
		assertEquals(original.id, deserialized.id);
		assertEquals(original.value, deserialized.value);
	}
	
	@Test
	public void testSerializeDeserializeWithBase64() {
		var serialization = new ObjectSerialization();
		var original = new TestObject("Base64", 200, 1.41, List.of(), List.of(), Map.of());
		
		var serialized = serialization.serialize(original, false, true, false, null, digest -> {
		});
		
		assertNotNull(serialized);
		assertTrue(serialized.length > 0);
		assertTrue(new String(serialized).matches("[A-Za-z0-9+/=]+"));
		
		var deserialized = serialization.deserialize(TestObject.class, serialized, false, false, true, null, null, result -> {
		});
		
		assertNotNull(deserialized);
		assertEquals(original.name, deserialized.name);
		assertEquals(original.id, deserialized.id);
		assertEquals(original.value, deserialized.value);
	}
	
	@Test
	public void testSerializeDeserializeWithDigest() {
		var serialization = new ObjectSerialization();
		var original = new TestObject("Digested", 300, 0.5, List.of("x"), List.of(), Map.of());
		var digestSalt = "salt".getBytes();
		var capturedDigest = new byte[32][];
		
		var serialized = serialization.serialize(original, false, false, true, digestSalt, digest -> capturedDigest[0] = digest);
		
		assertNotNull(serialized);
		assertNotNull(capturedDigest[0]);
		assertTrue(capturedDigest[0].length == 32);
		
		var digestCheckPassed = new boolean[1];
		var deserialized = serialization.deserialize(TestObject.class, serialized, false, false, false, capturedDigest[0], digestSalt, result -> digestCheckPassed[0] = result);
		
		assertNotNull(deserialized);
		assertTrue(digestCheckPassed[0]);
		assertEquals(original.name, deserialized.name);
		assertEquals(original.id, deserialized.id);
		assertEquals(original.value, deserialized.value);
	}
	
	@Test
	public void testSerializeDeserializeWithCompressionAndBase64() {
		var serialization = new ObjectSerialization();
		var original = new TestObject("CompressedBase64", 400, 1.23, List.of("p", "q"), List.of(new TestObject2("nested", true)), Map.of("key", 99));
		
		var serialized = serialization.serialize(original, true, true, false, null, digest -> {
		});
		
		assertNotNull(serialized);
		assertTrue(serialized.length > 0);
		assertTrue(new String(serialized).matches("[A-Za-z0-9+/=]+"));
		
		var deserialized = serialization.deserialize(TestObject.class, serialized, false, true, true, null, null, result -> {
		});
		
		assertNotNull(deserialized);
		assertEquals(original.name, deserialized.name);
		assertEquals(original.id, deserialized.id);
		assertEquals(original.value, deserialized.value);
	}
	
	@Test
	public void testSerializeDeserializeWithAllOptions() {
		var serialization = new ObjectSerialization();
		var original = new TestObject("AllOptions", 500, 9.99, List.of("all"), List.of(new TestObject2("a", false), new TestObject2("b", true)), Map.of("x", 1, "y", 2));
		var digestSalt = "salt123".getBytes();
		var capturedDigest = new byte[32][];
		
		var serialized = serialization.serialize(original, true, true, true, digestSalt, digest -> capturedDigest[0] = digest);
		
		assertNotNull(serialized);
		assertNotNull(capturedDigest[0]);
		assertTrue(new String(serialized).matches("[A-Za-z0-9+/=]+"));
		
		var digestCheckPassed = new boolean[1];
		var deserialized = serialization.deserialize(TestObject.class, serialized, false, true, true, capturedDigest[0], digestSalt, result -> digestCheckPassed[0] = result);
		
		assertNotNull(deserialized);
		assertTrue(digestCheckPassed[0]);
		assertEquals(original.name, deserialized.name);
		assertEquals(original.id, deserialized.id);
		assertEquals(original.value, deserialized.value);
	}
	
	public record TestObject(String name, int id, double value, List<String> tags, List<TestObject2> children, Map<String, Integer> attributes) {
	}
	
	public record TestObject2(String key, boolean flag) {
	}
	
}
