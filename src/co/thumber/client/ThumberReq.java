package co.thumber.client;

public class ThumberReq extends ThumberTransaction {
	
	/**
	 * Constructs new ThumberReq instance.
	 */
	public ThumberReq() {
	}
	
	/**
	 * Constructs new ThumberReq instance.
	 * 
	 * @param JSON string to populate instance fields.
	 */
	public ThumberReq(String json) {
		super(json);
	}
	
	/**
	 * @var The UID for API user.
	 */
	protected String uid;

	/**
	 * Sets the UID.
	 * 
	 * @param uid The UID.
	 */
	@JsonSetter("uid")
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * Gets the UID.
	 * 
	 * @return The UID.
	 */
	@JsonGetter("uid")
	public String getUid() {
		return uid;
	}

	/**
	 * @var The local URL that will be POSTed to with generated thumbnail. 
	 */
	protected String callback;

	/**
	 * Sets the callback URL.
	 * 
	 * @param callback The callback URL.
	 */
	@JsonSetter("callback")
	public void setCallback(String callback) {
		this.callback = callback;
	}

	/**
	 * Gets the callback URL.
	 * 
	 * @return The callback URL.
	 */
	@JsonGetter("callback")
	public String getCallback() {
		return callback;
	}

	/**
	 * @var URL pointing to the file to be thumbed.
	 */
	protected String url;

	/**
	 * Sets the URL pointing to the file to be thumbed.
	 * 
	 * @param url The URL.
	 */
	@JsonSetter("url")
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the URL pointing to the file to be thumbed.
	 * 
	 * @return The URL.
	 */
	@JsonGetter("url")
	public String getUrl() {
		return url;
	}

	/**
	 * The geometry string to use when thumbnail is being sized. 
	 * Geometry string should be a valid ImageMagick geometry string 
	 * (http://www.imagemagick.org/script/command-line-processing.php#geometry).
	 * 
	 * @var Geometry string for sizing thumbnail.
	 */
	protected String geometry;

	/**
	 * Sets the geometry string.
	 * 
	 * @param geometry The geometry string.
	 */
	@JsonSetter("geometry")
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}

	/**
	 * Gets the geometry string.
	 * 
	 * @return The geometry string.
	 */
	@JsonGetter("geometry")
	public String getGeometry() {
		return geometry;
	}

	/**
	 * 1-indexed page number to be used for generated thumbnail.
	 * 
	 * @var Page number to be used for generated thumbnail.
	 */
	protected int pg;

	/**
	 * Sets the page number to be used for generated thumbnail.
	 * 
	 * @param pg Page number.
	 */
	@JsonSetter("pg")
	public void setPg(int pg) {
		this.pg = pg;
	}

	/**
	 * Gets the page number to be used for generated thumbnail.
	 * 
	 * @return Page number.
	 */
	@JsonGetter("pg")
	public int getPg() {
		return pg;
	}

	/**
	 * @var The mime type of the file being thumbed.
	 */
	protected String mimeType;

	/**
	 * Sets the mime type.
	 * 
	 * @param mimeType The mime type.
	 */
	@JsonSetter("mime_type")
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Gets the mime type.
	 * 
	 * @return The mime type.
	 */
	@JsonGetter("mime_type")
	public String getMimeType() {
		return mimeType;
	}

	/* (non-Javadoc)
	 * @see co.thumber.client.ThumberTransaction#isValid(java.lang.String)
	 */
	public boolean isValid(String secret) {
		return super.isValid(secret) && isReqValid();
	}
	
	/* (non-Javadoc)
	 * @see co.thumber.client.ThumberTransaction#isValid()
	 */
	public boolean isValid() {
		return super.isValid() && isReqValid();
	}
	
	/**
	 * @return Whether fields specifc to request are valid.
	 */
	private boolean isReqValid() {
		return uid != null &&
				callback != null &&
				(url != null || getEncodedData() != null) &&
				mimeType != null;
	}
}
