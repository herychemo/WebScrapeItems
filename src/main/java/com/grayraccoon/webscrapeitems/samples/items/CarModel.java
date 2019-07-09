package com.grayraccoon.webscrapeitems.samples.items;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
class CarModel {
	private String title;
	private String year;
	private String photo;
}
