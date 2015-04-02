package co.thumber.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public abstract class ThumberTransaction {
	
	/**
	 * The cached MD5 digest object.
	 */
	private static MessageDigest md5 = null;
	
	/**
	 * The Base64 encoder.
	 */
	private static final Encoder encoder = Base64.getEncoder();
	
	/**
	 * The Base64 decoder.
	 */
	private static final Decoder decoder = Base64.getDecoder();

	/**
	 * Cached JSON getters referenced during serialization to JSON strings.
	 */
	private static final Map<Class<? extends ThumberTransaction>, Map<String, Method>> jsonGetters =
			new HashMap<Class<? extends ThumberTransaction>, Map<String, Method>>();

	/**
	 * Cached JSON setters referenced during deserialization of JSON strings.
	 */
	private static final Map<Class<? extends ThumberTransaction>, Map<String, Method>> jsonSetters =
			new HashMap<Class<? extends ThumberTransaction>, Map<String, Method>>();
	
	/**
	 * Perform class-level initialization that need only happen once.
	 */
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Failed to get MD5 MessageDigest. Attempting auto-generation of NONCE will fail.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructs new ThumberTransaction instance.
	 */
	public ThumberTransaction() {
	}

	/**
	 * Constructs new ThumberTransaction instance.
	 * 
	 * @param json JSON string for populating instance.
	 */
	public ThumberTransaction(String json) {
		// reflection is expensive -- only do it once per class and cache results
		Class<? extends ThumberTransaction> c = getClass();
		Map<String, Method> classSetters = jsonSetters.get(c);
		if (classSetters == null) {
			initJsonGettersSetters(c);
			classSetters = jsonSetters.get(c);
		}
		
		// go through each JSON keys and invoke the associated setter
		JSONObject parsed = (JSONObject)JSONValue.parse(json);
		for (Object key : parsed.keySet()) {
			Method setter = classSetters.get(key);
			if (setter == null) {
				System.err.println("Invalid JSON key skipped: " + key);
				continue;
			}
			
			try {
				setter.invoke(this, parsed.get(key));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.err.println("Failed to set field from JSON: " + key);
				e.printStackTrace();
			}
		}
	}

	/**
	 * @var The unique identifier for this transaction set (same used in both req & resultant resp).
	 */
	protected  String nonce;

	/**
	 * Sets the NONCE.
	 * 
	 * @param nonce The NONCE.
	 */
	@JsonSetter("nonce")
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	/**
	 * Sets the NONCE to a value derived from currentTimeMillis().
	 */
	public void setNonce() {
		byte[] digest = md5.digest(Long.toString(System.currentTimeMillis()).getBytes());
		nonce = new BigInteger(1, digest).toString(16);
	}

	/**
	 * Gets the NONCE.
	 * 
	 * @return The NONCE.
	 */
	@JsonGetter("nonce")
	public String getNonce() {
		return nonce;
	}

	/**
	 * @var The UTC timestamp representing when this transaction was sent.
	 */
	protected int timestamp;

	/**
	 * Sets the timestamp.
	 * 
	 * @param timestamp The timestamp.
	 */
	@JsonSetter("timestamp")
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the timestamp.
	 * 
	 * @return The timestamp.
	 */
	@JsonGetter("timestamp")
	public int getTimestamp() {
		return timestamp;
	}

	/**
	 * @var The checksum which is calculated with the contents of the 
	 * transaction (minus the checksum) and the user's secret with the HMAC-SHA256 algorithm.
	 */
	protected String checksum;

	/**
	 * Sets the checksum.
	 * 
	 * @param $checksum The checksum.
	 */
	@JsonSetter("checksum")
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * Gets the checksum.
	 * 
	 * @return The checksum.
	 */
	@JsonGetter("checksum")
	public String getChecksum() {
		return checksum;
	}

	/**
	 * The base64-encoded data.
	 * 
	 * @var base64-encoded data.
	 */
	protected String data;

	/**
	 * Sets the base64-encoded data.
	 * 
	 * @param data The base64-encoded data.
	 */
	@JsonSetter("data")
	public void setEncodedData(String data) {
		this.data = data;
		this.decodedData = null;
	}

	/**
	 * Gets the base64-encoded data.
	 * 
	 * NOTE: If only raw data is initialized, this method will populate the base64-encoded data from that value.
	 * 
	 * @return The base64-encoded data.
	 */
	@JsonGetter("data")
	public String getEncodedData() {
		if (data == null && decodedData != null) {
			data = encoder.encodeToString(decodedData);
		}

		return data;
	}

	/**
	 * The raw file data.
	 * 
	 * @var Raw data read from file.
	 */
	private byte[] decodedData;

	/**
	 * Gets the raw file data.
	 * 
	 * @param decodedData The raw file data.
	 */
	public void setDecodedData(byte[] decodedData) {
		this.decodedData = decodedData;
		this.data = null;
	}

	/**
	 * Gets the raw file data.
	 * 
	 * NOTE: If only base64 data is initialized, this method will populate the raw data from that value.
	 * 
	 * @return The raw file data.
	 */
	public byte[] getDecodedData() {
		if (decodedData == null && data != null) {
			decodedData = decoder.decode(data);
		}

		return decodedData;
	}

	/**
	 * Whether this instance is valid. Validity will include checksum validation.
	 * 
	 * @param secret The user secret.
	 * @return Whether this instance is valid.
	 */
	public boolean isValid(String secret) {
		return isValid() && isValidChecksum(secret);
	}

	/**
	 * Whether this instance is valid. No checksum validation is performed.
	 * 
	 * @return Whether this instance is valid.
	 */
	public boolean isValid() {
		return nonce != null && timestamp != 0 && checksum != null;
	}

	/**
	 * Computes checksum for this instance and compares against the value set as the instance checksum.
	 * 
	 * @param secret The user secret.
	 * @return Whether this instance's checksum value is valid for this instance's contents.
	 */
	public boolean isValidChecksum(String secret) {
		return computeChecksum(secret).equals(checksum);
	}

	/**
	 * Computes checksum based on instance variables.
	 * 
	 * @param secret The user secret.
	 * @return The checksum representing this instance.
	 */
	public String computeChecksum(String secret) {
		String ret = "";
		TreeMap<String, String> sanitized = new TreeMap<String, String>();
		
		// only use up to the first 1024 characters of each value in computing checksum
		// encode any special characters in value
		Map<String, Object> unclean = toMap();
		for (String key : unclean.keySet()) {
			// can't include checksum value when computing checksum
			if (key == "checksum") continue;
			
			String value = unclean.get(key).toString();
			sanitized.put(key, value.substring(0, Math.min(value.length(), 1024)));
		}

		final String HMAC_SHA256 = "HmacSHA256";
		String query = mapToString("=", "&", sanitized);
		
		try {
			Mac sha256 = Mac.getInstance(HMAC_SHA256);
			sha256.init(new SecretKeySpec(secret.getBytes(), HMAC_SHA256));
			byte[] hmac = sha256.doFinal(query.getBytes());
			ret = new BigInteger(1, hmac).toString(16);
		} catch (Exception e) {
			System.err.println("Failed to compute checksum:");
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public String toJson() {
		return JSONObject.toJSONString(toMap());
	}
	
	/**
	 * Gets Map representation of all non-null values returned from JsonGetters.
	 * 
	 * @return Map representation of this instance.
	 */
	private Map<String, Object> toMap() {
		// reflection is expensive -- only do it once per class and cache results
		Class<? extends ThumberTransaction> c = getClass();
		Map<String, Method> classGetters = jsonGetters.get(c);
		if (classGetters == null) {
			initJsonGettersSetters(c);
			classGetters = jsonGetters.get(c);
		}

		// go through all getters and 
		Map<String, Object> ret = new HashMap<String, Object>();
		for (String name : classGetters.keySet()) {
			Object value = null;
			try {
				value = classGetters.get(name).invoke(this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
			// skip all null values
			if (value != null) {
				ret.put(name, value);
			}
		}
		
		return ret;
	}
	
	/**
	 * Initializes the Maps for JSON getters/setters for the given class.
	 * 
	 * @param c The class to init JSON getters and setters from.
	 */
	private void initJsonGettersSetters(Class<? extends ThumberTransaction> c) {
		Map<String, Method> classGetters = new HashMap<String, Method>();
		Map<String, Method> classSetters = new HashMap<String, Method>();
		
		// traverse up the inheritance chain, collecting getters/setters as we go
		Class<?> clazz = c;
		while (!Object.class.equals(clazz) && clazz != null) {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.isAnnotationPresent(JsonGetter.class)) {
					m.setAccessible(true);
					JsonGetter jg = m.getAnnotation(JsonGetter.class);
					classGetters.put(jg.value(), m);
				} else if (m.isAnnotationPresent(JsonSetter.class)) {
					m.setAccessible(true);
					JsonSetter js = m.getAnnotation(JsonSetter.class);
					classSetters.put(js.value(), m);
				}
			}
			
			clazz = clazz.getSuperclass();
		}
		
		jsonGetters.put(c, classGetters);
		jsonSetters.put(c, classSetters);
	}
	
	/**
	 * @param inner The inner glue.
	 * @param outer The outer glue.
	 * @param map The Map to be joined into a String.
	 * @return The Map, imploded into String.
	 */
	private <K, V> String mapToString(String inner, String outer, Map<K, V> map) {
		if (map.isEmpty()) return "";
		
		StringBuilder ret = new StringBuilder();
		for (K k : map.keySet()) {
			ret.append(k);
			ret.append(inner);
			ret.append(map.get(k));
			ret.append(outer);
		}
		
		return ret.substring(0, ret.length() - outer.length());
	}
	
	/**
	 * Annotation which marks all field getters to be included when serializing to JSON.
	 */
	@Inherited
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	protected @interface JsonGetter {
		/**
		 * The key to use when generating JSON key-value pair.
		 */
		String value();
	}
	
	/**
	 * Annotation which marks all field setters to be included when deserializing from JSON.
	 */
	@Inherited
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	protected @interface JsonSetter {
		/**
		 * The key to use when generating JSON key-value pair.
		 */
		String value();
	}
}
