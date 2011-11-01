package plotter.printing;

import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

public class JobListener implements PrintJobListener {

	private PrintJob job;
	private boolean completed = false;

	public JobListener(PrintJob job) {
		this.job = job;
	}

	@Override
	public void printDataTransferCompleted(PrintJobEvent event) {}

	@Override
	public void printJobCanceled(PrintJobEvent event) {}

	@Override
	public void printJobCompleted(PrintJobEvent event) {
		completed = true;

		job.finished(true);
	}

	@Override
	public void printJobFailed(PrintJobEvent event) {
		job.finished(false);
	}

	@Override
	public void printJobNoMoreEvents(PrintJobEvent event) {
		if ( ! completed)
			job.finished(true);
	}

	@Override
	public void printJobRequiresAttention(PrintJobEvent event) {}

}
