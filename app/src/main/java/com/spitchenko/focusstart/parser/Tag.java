package com.spitchenko.focusstart.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Date: 01.03.17
 * Time: 22:55
 *
 * @author anatoliy
 */
class Tag {
	@Getter @Setter String name;
	@Getter @Setter String text;
	@Getter @Setter int depth;
	@Getter @Setter Tag parent;
	@Getter @Setter List<Tag> children = new ArrayList<>();
	@Getter @Setter Map<String, String> attributes = new HashMap<>();

	Tag(String name, Tag parent, int depth) {
		this.name = name;
		this.parent = parent;
		this.depth = depth;
	}
}