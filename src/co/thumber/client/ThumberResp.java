package co.thumber.client;

public class ThumberResp extends ThumberTransaction {

	/**
	 * Constructs new ThumberResp instance.
	 */
	public ThumberResp() {
	}
	
	/**
	 * Constructs new ThumberResp instance.
	 * 
	 * @param JSON string to populate instance fields.
	 */
	public ThumberResp(String json) {
		super(json);
	}

	/**
	 * @var Whether the related ThumberReq was successful.
	 */
	protected boolean success;

	/**
	 * Sets whether the related ThumberReq was successful.
	 * 
	 * @param success Whether the related ThumberReq was successful.
	 */
	@JsonSetter("success")
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Gets whether the related ThumberReq was successful.
	 * 
	 * @return Whether the related ThumberReq was successful.
	 */
	@JsonGetter("success")
	public boolean getSuccess() {
		return success;
	}

	/**
	 * The error string indicating what went wrong.
	 * 
	 * @var The error string.
	 */
	protected String error;

	/**
	 * Sets the error string indicating what went wrong.
	 * 
	 * @param error The error string.
	 */
	@JsonSetter("error")
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Gets the error string indicating what went wrong.
	 * 
	 * @return The error string.
	 */
	@JsonGetter("error")
	public String getError() {
		return error;
	}

	/* (non-Javadoc)
	 * @see co.thumber.client.ThumberTransaction#isValid()
	 */
	public boolean isValid() {
		return super.isValid() && isRespValid();
	}

	/* (non-Javadoc)
	 * @see co.thumber.client.ThumberTransaction#isValid(java.lang.String)
	 */
	public boolean isValid(String secret) {
		return super.isValid(secret) && isRespValid();
	}
	
	/**
	 * @return Whether fields specifc to response are valid.
	 */
	private boolean isRespValid() {
		return success ? getEncodedData() != null : error != null;
	}
}
