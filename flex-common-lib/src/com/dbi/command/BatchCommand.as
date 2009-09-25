package com.dbi.command
{
	import com.adobe.cairngorm.control.CairngormEvent;
	import com.adobe.cairngorm.control.CairngormEventDispatcher;
	
	import mx.collections.ArrayCollection;
	
	/**
	 * <p>A BatchCommand can be used to specify a set of CairngormEvents that after completion will
	 * fire a different set of CairngormEvents.  You must register the set of events to fire with a
	 * unique job name along with the set of events that trigger the job.  Once a job has
	 * all of its trigger events complete it will execute its associated events and remove
	 * the job from the list.</p>
	 * 
	 * <p>All commands that wish to participate in a batch must extend BatchCommand and specify
	 * the commandEventName that executes the command.  They must also call commandComplete()
	 * when the command has completed execution.</p>
	 * 
	 * @author khoyt2
	 */ 
	public class BatchCommand
	{
		private static var eventList:ArrayCollection = new ArrayCollection();
		
		/**
		 * CairngormEvent that is associated with this command
		 */
		public var commandEventName:String;
		
		public function BatchCommand()
		{
		}

		/**
		 * Remove all batch jobs 
		 */		
		public static function clearAllBatchJobs():void
		{
			eventList = new ArrayCollection();
		}
		
		/**
		 * Must be called by the subclass to ensure its participation in a batch job
		 */
		public function commandComplete():void
		{
			BatchCommand.finishBatchEvent(commandEventName)
		}
		
		/**
		 * Adds or replaces a batch with the ID of "job" to the job list and
		 * sets the triggerEvents to a not fired status.
		 * 
		 * @param job Unique ID to identifiy this job
		 * @param triggerEvents String Array of event names that will trigger the fireEvents when
		 * 						  all have been completed.
		 * @param fireEvents Array of CairngormEvents that will be dispatched when their trigger events
		 * 						have completed.
		 */
		public static function addBatchCommandJob(job:String, triggerEvents:Array, fireEvents:Array):void
		{
			// Create batch job in the eventMap
			var item:Object = new Object();
			
			// Store set of events to fire
			item.fireEvents = fireEvents;
			
			// Store set of events to trigger
			item.triggerEvents = new ArrayCollection();
			for each (var trigger:String in triggerEvents)
			{
				var triggerEvent:Object = new Object();
				triggerEvent.eventName = trigger;
				triggerEvent.complete = false;
				item.triggerEvents.addItem(triggerEvent);
			}
			eventList.addItem(item);
		}
		
		/**
		 * Finds events in any current job with the given event name
		 * and marks them as being completed
		 */
		private static function finishBatchEvent(eventName:String):void
		{
			if (eventName != null)
			{
				for each (var job:Object in eventList)
				{
					for each (var triggerEvent:Object in job.triggerEvents)
					{
						if (triggerEvent.eventName == eventName)
							triggerEvent.complete = true;
					}
				}
			}
			checkBatchJobs();
		}
		
		/**
		 * Finds completed jobs in the job list and fires their associated
		 * events then removes the job from the queue
		 */
		private static function checkBatchJobs():void
		{
			// Go through each job and check if the batch of trigger events have fired
			for (var i:int = eventList.length - 1; i >= 0; i--)
			{
				var job:Object = eventList[i];
				var jobFinished:Boolean = true;
				for each (var triggerEvent:Object in job.triggerEvents)
				{
					if (!triggerEvent.complete)
					{
						jobFinished = false;
						break;
					}
				}
				
				// If the batch is finished fire each event in the fireEvents collection
				if (jobFinished)
				{
					for each (var event:CairngormEvent in job.fireEvents)
					{
						CairngormEventDispatcher.getInstance().dispatchEvent(event);
					}
					eventList.removeItemAt(i);
				}
			}
		}
	}
}