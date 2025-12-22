package de.extio.game_engine.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Consumer;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

public final class ObjectSerialization {
	
	private static final ObjectMapper OBJECT_MAPPER;
	
	static {
		OBJECT_MAPPER = YAMLMapper.builder()
				.findAndAddModules()
				.build();
	}
	
	public static byte[] serialize(final Object o, final boolean compress, final boolean base64, final boolean digest, final byte[] digestSalt, final Consumer<byte[]> digestConsumer) {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1 << 16);
		serialize(o, byteArrayOutputStream, compress, base64, digest, digestSalt, digestConsumer);
		return byteArrayOutputStream.toByteArray();
	}
	
	public static void serialize(final Object o, final OutputStream outputStream, final boolean compress, final boolean base64, final boolean digest, final byte[] digestSalt, final Consumer<byte[]> digestConsumer) {
		OutputStream base64OutputStream = null;
		ZstdOutputStream zstdOutputStream = null;
		DigestOutputStream digestOutputStream = null;
		try {
			OutputStream stream = outputStream;
			if (base64) {
				stream = base64OutputStream = Base64.getEncoder().wrap(stream);
			}
			if (compress) {
				stream = zstdOutputStream = new ZstdOutputStream(stream);
			}
			if (digest) {
				stream = digestOutputStream = new DigestOutputStream(stream, MessageDigest.getInstance("SHA-256"));
			}
			OBJECT_MAPPER.writeValue(stream, o);
		}
		catch (IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		finally {
			if (digestOutputStream != null) {
				try {
					digestOutputStream.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				digestConsumer.accept(digestOutputStream.getMessageDigest().digest(digestSalt));
			}
			if (zstdOutputStream != null) {
				try {
					zstdOutputStream.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			if (base64OutputStream != null) {
				try {
					base64OutputStream.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}
	
	public static <T extends Object> T deserialize(final Class<T> clazz, final byte[] data, final boolean decompress, final boolean base64, final byte[] checkDigest, final byte[] digestSalt, final Consumer<Boolean> digestCheckConsumer) {
		return deserialize(clazz, new ByteArrayInputStream(data), decompress, base64, checkDigest, digestSalt, digestCheckConsumer);
	}
	
	public static <T extends Object> T deserialize(final Class<T> clazz, final InputStream inputStream, final boolean decompress, final boolean base64, final byte[] checkDigest, final byte[] digestSalt, final Consumer<Boolean> digestCheckConsumer) {
		InputStream base64InputStream = null;
		ZstdInputStream zstdInputStream = null;
		DigestInputStream digestInputStream = null;
		try {
			InputStream stream = inputStream;
			if (base64) {
				stream = base64InputStream = Base64.getDecoder().wrap(stream);
			}
			if (decompress) {
				stream = zstdInputStream = new ZstdInputStream(stream);
			}
			if (checkDigest != null && checkDigest.length > 0) {
				stream = digestInputStream = new DigestInputStream(stream, MessageDigest.getInstance("SHA-256"));
			}
			
			return OBJECT_MAPPER.readValue(stream, clazz);
		}
		catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		finally {
			if (digestInputStream != null) {
				try {
					digestInputStream.close();
					digestCheckConsumer.accept(Arrays.equals(checkDigest, digestInputStream.getMessageDigest().digest(digestSalt)));
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			if (zstdInputStream != null) {
				try {
					zstdInputStream.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			if (base64InputStream != null) {
				try {
					base64InputStream.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}
	
}
