package plotter.pdf;

import java.util.Date;

import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

public class JobListener implements PrintJobListener {

	PrintJob job;

	public JobListener(PrintJob job) {
		System.out.println(new Date() + ": JobListener created");

		this.job = job;
	}

	@Override
	public void printDataTransferCompleted(PrintJobEvent event) {
		System.out.println(new Date() + ": printDataTransferCompleted");
	}

	@Override
	public void printJobCanceled(PrintJobEvent event) {}

	@Override
	public void printJobCompleted(PrintJobEvent event) {
		System.out.println(new Date() + ": printJobCompleted");
	}

	@Override
	public void printJobFailed(PrintJobEvent event) {
		// TODO Add status field to Document with Enum completed & failed to represent this
	}

	@Override
	public void printJobNoMoreEvents(PrintJobEvent event) {
		System.out.println(new Date() + ": printJobNoMoreEvents");
	}

	@Override
	public void printJobRequiresAttention(PrintJobEvent event) {}

}
