import org.apache.commons.lang3.builder.ToStringBuilder;

public class CarModel {

	private String title;

	private String year;

	private String photo;

	public CarModel() {}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("title", title)
				.append("year", year)
				.append("photo", photo)
				.toString();
	}
}
