package com.dbi.cat.view.address
{
	import mx.controls.DataGrid;

	[Event(name="selectAll", type="flash.events.Event")]
	[Event(name="unselectAll", type="flash.events.Event")]
	public class ImportDataGrid extends DataGrid
	{
		public function ImportDataGrid()
		{
			super();
		}
		
	}
}