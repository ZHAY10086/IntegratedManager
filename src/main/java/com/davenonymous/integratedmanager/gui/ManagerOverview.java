package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.overview.ManagerPanel;
import com.davenonymous.integratedmanager.gui.search.ElementSearchables;
import com.davenonymous.integratedmanager.gui.search.WidgetBadge;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import com.davenonymous.integratedmanager.lib.gui.GUI;
import com.davenonymous.integratedmanager.lib.gui.WidgetFullScreen;
import com.davenonymous.integratedmanager.lib.gui.event.GuiDataUpdatedEvent;
import com.davenonymous.integratedmanager.lib.gui.event.MouseClickEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.widgets.*;
import com.davenonymous.integratedmanager.networking.NetworkDataRequest;
import com.davenonymous.integratedmanager.setup.config.DebugConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManagerOverview extends WidgetFullScreen {
	int sidePadding = 16;

	WidgetPlayButton playButton;
	WidgetImage refreshButton;
	ManagerPanel managerPanel;
	WidgetProgressBar progressBar;


	private Set<ElementSearchables> includeInSearch = new HashSet<>(List.of(
		ElementSearchables.PARTS,
		ElementSearchables.VALUES,
		ElementSearchables.VARIABLES,
		ElementSearchables.TILES,
		ElementSearchables.ASPECTS,
		ElementSearchables.IDS
	));
	WidgetNativeWidget<EditBox> searchField;
	private WidgetBadge caseSensitiveBadge;
	private WidgetBadge searchRegexBadge;
	private WidgetBadge searchVariablesBadge;
	private WidgetBadge searchPartsBadge;
	private WidgetBadge searchValuesBadge;
	private WidgetBadge searchTilesBadge;
	private WidgetBadge searchIDsBadge;
	private WidgetBadge searchAspectsBadge;

	public String searchString = "";
	private boolean searchCompareCase = false;
	private boolean searchWithRegex = false;
	private WidgetTextBox titleLabel;

	public ManagerOverview() {
		super(Component.translatable("itemGroup.integratedmanager"));

		// this.setShrinkWidth(160);
	}

	private void triggerSearchUpdate() {
		managerPanel.runSearch(
			searchString,
			searchCompareCase,
			searchWithRegex,
			includeInSearch
		);
	}

	protected WidgetNativeWidget<EditBox> createSearchBar(GUI gui) {
		var editBox = new EditBox(Minecraft.getInstance().font, 0, 0, Component.translatable("fml.menu.mods.search"));
		editBox.setPosition(2, 2);
		editBox.setValue("");
		editBox.setEditable(true);
		editBox.active = true;
		editBox.setBordered(false);
		editBox.setTextColor(0x5c5c5c);
		editBox.setTextShadow(false);
		editBox.setHint(Component.translatable("fml.menu.mods.search"));
		editBox.setResponder(queryString -> {
			if(queryString.isBlank() || !editBox.isFocused()) {
				editBox.setTextColor(0x5c5c5c);
			} else {
				editBox.setTextColor(ChatFormatting.WHITE.getColor());
			}
			this.searchString = queryString;
			triggerSearchUpdate();
		});

		searchField = new WidgetNativeWidget<>(editBox);
		searchField.setDrawBackground(true);
		searchField.setPosition(4, -2);
		searchField.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(!event.isLeftClick()) {
					editBox.setValue("");
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		caseSensitiveBadge = new WidgetBadge(I18n.get("integratedmanager.searchtoggles.case"), 0xFF173f5f);
		caseSensitiveBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				searchCompareCase = !searchCompareCase;
				caseSensitiveBadge.active = searchCompareCase;
				triggerSearchUpdate();
				return WidgetEventResult.HANDLED;
			}
		);
		caseSensitiveBadge.active = searchCompareCase;
		caseSensitiveBadge.first = true;
		caseSensitiveBadge.setVisible(false);
		caseSensitiveBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.case.info"));
		gui.add(caseSensitiveBadge);

		searchRegexBadge = new WidgetBadge(I18n.get("integratedmanager.searchtoggles.regex"), 0xFFF6D55C);
		searchRegexBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				searchWithRegex = !searchWithRegex;
				searchRegexBadge.active = searchWithRegex;
				triggerSearchUpdate();
				return WidgetEventResult.HANDLED;
			}
		);
		searchRegexBadge.active = searchWithRegex;
		searchRegexBadge.setVisible(false);
		searchRegexBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.regex.info"));
		gui.add(searchRegexBadge);


		searchVariablesBadge = easyBadge(I18n.get("integratedmanager.searchtoggles.variables"), ColorHelper.COLOR_CYAN, ElementSearchables.VARIABLES);
		searchVariablesBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.variables.info"));
		gui.add(searchVariablesBadge);

		searchPartsBadge = easyBadge(I18n.get("integratedmanager.searchtoggles.part"), ColorHelper.COLOR_ERRORED.getRGB(), ElementSearchables.PARTS);
		searchPartsBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.part.info"));
		gui.add(searchPartsBadge);

		searchTilesBadge = easyBadge(I18n.get("integratedmanager.searchtoggles.tile"), ColorHelper.COLOR_ERRORED.getRGB(), ElementSearchables.TILES);
		searchTilesBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.tile.info"));
		gui.add(searchTilesBadge);

		searchIDsBadge = easyBadge(I18n.get("integratedmanager.searchtoggles.id"),ColorHelper.COLOR_ERRORED.getRGB(), ElementSearchables.IDS);
		searchIDsBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.id.info"));
		gui.add(searchIDsBadge);

		searchAspectsBadge = easyBadge(I18n.get("integratedmanager.searchtoggles.aspects"), ColorHelper.COLOR_GREEN, ElementSearchables.ASPECTS);
		searchAspectsBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.aspects.info"));
		gui.add(searchAspectsBadge);

		searchValuesBadge = easyBadge(I18n.get("integratedmanager.searchtoggles.value"), ColorHelper.COLOR_ORANGE, ElementSearchables.VALUES);
		searchValuesBadge.last = true;
		searchValuesBadge.setTooltipLines(Component.translatable("integratedmanager.searchtoggles.value.info"));
		gui.add(searchValuesBadge);

		return searchField;
	}

	protected void updateWidgetSizes() {
		sidePadding = 8;
		refreshButton.setPosition(this.width / 2 - refreshButton.width / 2, 5);
		playButton.setPosition(this.width - 26 - sidePadding, 5);
		managerPanel.setDimensions(sidePadding, sidePadding + 16, this.width - (2*sidePadding), this.height - (2*sidePadding) - 16);

		int minSearchFieldWidth = caseSensitiveBadge.width + searchRegexBadge.width + searchAspectsBadge.width;
		minSearchFieldWidth += searchIDsBadge.width + searchPartsBadge.width + searchTilesBadge.width;
		minSearchFieldWidth += searchValuesBadge.width + searchVariablesBadge.width;

		int searchFieldX = titleLabel.x + titleLabel.width + 4;
		int maxSearchFieldWidth = this.width - searchFieldX - minSearchFieldWidth - 40;

		searchField.setPosition(titleLabel.x + titleLabel.width + 4, titleLabel.y + 4);
		searchField.setWidth(maxSearchFieldWidth);
		searchField.setHeight(12);
		searchField.setVisible(true);
		List<WidgetBadge> badges = List.of(
			caseSensitiveBadge,
			searchRegexBadge,
			searchVariablesBadge,
			searchPartsBadge,
			searchTilesBadge,
			searchAspectsBadge,
			searchIDsBadge,
			searchValuesBadge
		);

		int badgeX = searchField.x + searchField.width;
		for(Widget badge : badges) {
			badge.setPosition(badgeX, searchField.y);
			badge.setVisible(true);
			badgeX += badge.width; // Add some spacing between badges
		}
	}

	private WidgetBadge easyBadge(String badgeText, int badgeColor, ElementSearchables searchable) {
		var badge = new WidgetBadge(badgeText, badgeColor);
		badge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(includeInSearch.contains(searchable)) {
					includeInSearch.remove(searchable);
					badge.active = false;
				} else {
					includeInSearch.add(searchable);
					badge.active = true;
				}

				triggerSearchUpdate();
				return WidgetEventResult.HANDLED;
			}
		);
		badge.active = includeInSearch.contains(searchable);
		badge.setVisible(false);
		return badge;
	}

	@Override
	protected GUI createGUI() {
		GUI gui = super.createGUI();

		titleLabel = new WidgetTextBox(I18n.get("itemGroup.integratedmanager"));
		titleLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		titleLabel.setPosition(6, 6);
		titleLabel.autoWidth();
		gui.add(titleLabel);

		searchField = createSearchBar(gui);
		searchField.setPosition(this.width - searchField.width - 20, 6);
		gui.add(searchField);

		progressBar = new WidgetProgressBar();
		progressBar.setDisplayMode(WidgetProgressBar.EnumDisplayMode.NOTHING);
		progressBar.setPosition(6, 18);
		progressBar.setSize(titleLabel.width, 4);
		progressBar.setBorderColor(0x80000000);
		progressBar.setBackgroundColor(0x00000000);
		progressBar.setValue(66D);
		gui.add(progressBar);


		managerPanel = new ManagerPanel();
		gui.add(managerPanel);

		playButton = new WidgetPlayButton(false, () -> {
			managerPanel.freezeActivity(false);
		}, () -> {
			managerPanel.freezeActivity(true);
		});
		playButton.setEnabled(!DebugConfig.autoAdvanceGraph);
		playButton.setVisible(!DebugConfig.autoAdvanceGraph);
		gui.add(playButton);

		refreshButton = new WidgetImage(IntegratedManager.resource("textures/gui/arrowhead.png"));
		refreshButton.setSize(16, 16);
		refreshButton.addListener(MouseClickEvent.class, (event, widget) -> {
			if(event.button == 0) { // Left click
				PacketDistributor.sendToServer(new NetworkDataRequest(NetworkData.cache().masterPosition));
			}
			return WidgetEventResult.HANDLED;
		});
		// gui.add(refreshButton);

		gui.addListener(GuiDataUpdatedEvent.class, (event, widget) -> {
			int totalParts = NetworkData.cache().totalParts;
			if(totalParts <= 0) {
				totalParts = 1; // Avoid division by zero
			}
			progressBar.setRange(0d, (double)totalParts);
			progressBar.setValue((double)NetworkData.cache().elementDataList.size());
			progressBar.setTooltipLines(Component.literal("Elements: " + NetworkData.cache().elementDataList.size() + "/" + NetworkData.cache().totalParts));
			return WidgetEventResult.CONTINUE_PROCESSING;
		});

		gui.addListener(AllElementsReceivedEvent.class, (event, widget) -> {
			// managerPanel.centerOnCanvas();
			var cache = NetworkData.cache();
			return WidgetEventResult.CONTINUE_PROCESSING;
		});


		updateWidgetSizes();
		return gui;
	}
}
