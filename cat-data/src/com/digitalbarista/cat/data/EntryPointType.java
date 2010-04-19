package com.digitalbarista.cat.data;

public enum EntryPointType {
	SMS (0, "SMS", 160, 0),
	Email (1, "Email", 0, null),
	Twitter (2, "Twitter", 140, null),
	Facebook (3, "Facebook", 0, null);
	
	private Integer id;
	private String name;
	private Integer maxCharacters;
	private Integer defaultMessageCredits;  // NULL = Unlimited
	
	EntryPointType(Integer id, String name, Integer maxCharacters, Integer defaultMessageCredits)
	{
		this.id = id;
		this.name = name;
		this.maxCharacters = maxCharacters;
		this.defaultMessageCredits = defaultMessageCredits;
	}
	
	public static EntryPointType getById(Integer id)
	{
		return EntryPointType.values()[id];
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getMaxCharacters() {
		return maxCharacters;
	}

	public void setMaxCharacters(Integer maxCharacters) {
		this.maxCharacters = maxCharacters;
	}

	public Integer getDefaultMessageCredits() {
		return defaultMessageCredits;
	}

	public void setDefaultMessageCredits(Integer defaultMessageCredits) {
		this.defaultMessageCredits = defaultMessageCredits;
	}
	
	
}
