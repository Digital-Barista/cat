package com.digitalbarista.cat.util;

import java.util.Random;

public class SequentialBitShuffler {

	private static final char[] charSequence = 
	{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F',
	 'G','H','J','K','L','M','N','P','Q','R','T','U','V','W','X','Y'};
	
	private int[] shuffleSequence;
	private int charCount;
	
	public SequentialBitShuffler(int[] bitShuffleSequence, int charCount)
	{
		this.shuffleSequence = bitShuffleSequence;
		this.charCount = charCount;
		validateSequence();
	}
	
	private void validateSequence()
	{
		if(shuffleSequence==null)
			throw new IllegalArgumentException("Cannot have a null shuffle sequence.");
		
		if(shuffleSequence.length != (charCount*5))
			throw new IllegalArgumentException("There must be exactly 5 bits for every character.");

		boolean[] isValid = new boolean[shuffleSequence.length];
		
		for(int position : shuffleSequence)
		{
			if(position <0 || position >= shuffleSequence.length)
				throw new IllegalArgumentException("shuffle sequence contains the invalid position "+position);
			
			if(isValid[position])
				throw new IllegalArgumentException("position #"+position+" is repeated in the shuffle sequence.");

			isValid[position]=true;
		}
	}
	
	public String generateCode(long seed)
	{
		long shuffledNumber = shuffle(seed);
		return translate(shuffledNumber);
	}
	
	private long shuffle(long seed)
	{
		long result = 0;
		long temp;
		long currentPos = 1;
		for(int position : shuffleSequence)
		{
			temp = 1 << position;
			if((temp & seed) == temp)
				result = result | currentPos;
			currentPos = currentPos << 1;
		}
		return result;
	}
	
	private String translate(long fromNumber)
	{
		char[] word = new char[charCount];
		for(int loop=0; loop<charCount; loop++)
		{
			word[charCount-(loop+1)] = charSequence[(int)(fromNumber % 32)];
			fromNumber = fromNumber >> 5;
		}
		return new String(word);
	}
	
	public static int[] generateBitShuffle(int length)
	{
		int[] ret=new int[length*5];
		for(int loop=0; loop<ret.length; loop++)
			ret[loop]=-1;
		Random rnd = new Random();
		int pos;
		for(int loop=0; loop<ret.length; loop++)
		{
			pos = rnd.nextInt(ret.length);
			while(ret[pos]!=-1)
			{
				if(pos==0) pos=ret.length;
				pos--;
			}
			ret[pos]=loop;
		}
		return ret;
	}
}
