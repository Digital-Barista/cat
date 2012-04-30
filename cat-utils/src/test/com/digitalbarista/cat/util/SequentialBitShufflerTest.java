package com.digitalbarista.cat.util;

import java.util.ArrayList;

import org.testng.annotations.Test;

public class SequentialBitShufflerTest 
{
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testNullShuffleSequence()
	{
		new SequentialBitShuffler(null,0);
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testTooManySequenceNumbers()
	{
		new SequentialBitShuffler(new int[]{0,1,2,3,4,5},1);
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testTooFewSequenceNumbers()
	{
		new SequentialBitShuffler(new int[]{0,1,2,3},1);
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testNegativeSequenceNumbers()
	{
		new SequentialBitShuffler(new int[]{-1,1,2,3,4},1);
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testTooHighSequenceNumbers()
	{
		new SequentialBitShuffler(new int[]{1,2,3,4,5},1);
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testDuplicatedSequenceNumbers()
	{
		new SequentialBitShuffler(new int[]{1,1,2,3,4},1);
	}
	
	@Test
	public void testTwoChar()
	{
		int[] shuffleSequence = {2,5,3,8,6,7,0,9,1,4};
		SequentialBitShuffler sbs=new SequentialBitShuffler(shuffleSequence,2);

		long result;
		assert sbs.generateCode(1).equals("20"); //64
		assert sbs.generateCode(2).equals("80"); //256
		assert sbs.generateCode(3).equals("A0"); //320
		assert sbs.generateCode(4).equals("01"); //1
	}
	
	@Test
	public void visualThreeCharTest()
	{
		int[] shuffleSequence = {2,14,5,3,10,8,6,7,11,0,9,13,1,12,4};
		SequentialBitShuffler sbs=new SequentialBitShuffler(shuffleSequence,3);

		ArrayList<String> results=new ArrayList<String>();
		String result;
		for(long loop=0; loop<32768; loop++)
		{
			result = sbs.generateCode(loop);
			assert result!=null : "Result was null.  seed="+loop;
			assert result.length()==3 : "Result was not 3.  result="+result+" seed="+loop;
			assert !results.contains(result) : "Result has already been generated.  seed="+loop;
			results.add(result);
			//System.out.println(""+loop+"="+result);
		}
	}
}
