package plotter.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.json.JSONException;
import org.json.JSONObject;

@Entity()
@Table(name = "DOCUMENT")
public class Document {

	private Long id;
	private Date printDate;
	private String fileName;
	private String description;
	private User user;
	private Integer pageCount;
	private Integer copies;
	private String format;
	private Float price;

	public Document() {
	}

	public Document(String filename, String desc, String format,
			Integer pageCount, Integer copies, Float price, User u, Date printDate) {
		this.fileName = filename;
		this.description = desc;
		this.format = format;
		this.pageCount = pageCount;
		this.copies = copies;
		this.price = price;
		this.user = u;
		this.printDate = printDate;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "DOC_ID", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "DOC_DESC", nullable = true)
	public String getDescription() {
		return description;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	@Column(name = "DOC_PAGECOUNT")
	public Integer getPageCount() {
		return pageCount;
	}
	
	@Column(name = "DOC_COPIES")
	public Integer getCopies() {
		return copies;
	}

	public void setCopies(Integer copies) {
		this.copies = copies;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "USER_ID")
	public User getUser() {
		return user;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Column(name = "DOC_FORMAT")
	public String getFormat() {
		return format;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	@Column(name = "DOC_PRICE")
	public Float getPrice() {
		return price;
	}

	public void setPrintDate(Date printDate) {
		this.printDate = printDate;
	}

	@Column(name = "DOC_PRINTDATE")
	public Date getPrintDate() {
		return printDate;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "DOC_FILENAME")
	public String getFileName() {
		return fileName;
	}

	public String toString() {
		String result = "User " + this.user + " printed " + this.fileName
				+ " with description \"" + this.description + "\" and " + this.pageCount
				+ " pages on " + this.format + " for " + this.price;
		return result;
	}

	public JSONObject toJSON() throws JSONException {
		return toJSON(false);
	}

	public JSONObject toJSON(boolean includeUser) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("id", this.getId());
		object.put("filename", this.getFileName());
		object.put("pages", this.getPageCount());
		object.put("copies", this.getCopies());
		object.put("format", this.getFormat());
		object.put("date", this.getPrintDate().getTime());
		object.put("price", this.getPrice());
		object.put("status", "complete");

		if(includeUser) {
			object.put("username", this.getUser().getFirstName() + " " + this.getUser().getLastName());
		}

		return object;
	}
}
