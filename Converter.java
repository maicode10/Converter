package converter;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.Cursor;
import javafx.scene.Node;
import java.io.*;
import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.shape.Circle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import java.util.Base64;

/**
 * Main class for the Currency Converter application.
 * Extends JavaFX Application to create a GUI-based currency conversion tool.
 */
public class Converter extends Application {

    // Map to store currency codes and their exchange rates relative to USD
    private final Map<String, Double> rates = new LinkedHashMap<>();
    // DecimalFormat for formatting currency amounts with commas and two decimal places
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    // VBox to hold the history of conversions in the UI
    private VBox historyContainer;
    
    // File storage constants for saving and loading data
    private static final String HISTORY_FILE = "history.txt";
    private static final String ERROR_LOG_FILE = "error_log.txt";
    private static final String USER_BEHAVIOR_FILE = "user_behavior.txt";
    private static final String PRECISION_LOG_FILE = "precision_log.txt";
    
    // Configuration constant for the threshold above which conversions are logged for precision
    private static final double PRECISION_THRESHOLD = 10000.0;
    
    // Map to track usage statistics of currency pairs
    private final Map<String, Integer> currencyPairUsage = new LinkedHashMap<>();
    // DateTimeFormatter for formatting timestamps in logs
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Main entry point for the JavaFX application.
     * Sets up the UI, loads data, and displays the primary stage.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Sample hardcoded exchange rates for demonstration purposes
        rates.put("USD", 1.0000);
        rates.put("EUR", 0.85);
        rates.put("GBP", 0.73);
        rates.put("JPY", 155.8);
        rates.put("CAD", 1.38);
        rates.put("AUD", 0.66);
        rates.put("CHF", 0.80);
        rates.put("CNY", 7.08);
        rates.put("SEK", 10.6);
        rates.put("NZD", 1.51);
        rates.put("MXN", 18.3);
        rates.put("SGD", 1.37);
        rates.put("HKD", 7.79);
        rates.put("NOK", 8.50);
        rates.put("KRW", 1310.0);
        rates.put("TRY", 42.7);
        rates.put("PHP", 59.1);

        // Load user behavior data from file before UI setup
        loadUserBehavior();
        // ======================================================

        // Root layout using BorderPane for the main application window
        BorderPane root = new BorderPane();
        root.setPrefSize(1200, 760);

        // ================= HEADER =================
        VBox header = new VBox();
        header.setPrefHeight(260);

        // Gradient background for the header
        Stop[] stops = new Stop[]{
                new Stop(0, Color.web("#5b2c91")),
                new Stop(0.5, Color.web("#7a2ed6")),
                new Stop(1, Color.web("#8f39ff"))
        };

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops
        );

        header.setBackground(new Background(
                new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        // ---------------- TOP NAV ----------------
        HBox topNav = createTopNav(primaryStage);
        topNav.setPrefHeight(56);

        // ---------------- HERO CONTENT ----------------
        Label heroTitle = new Label("Convert Currencies Instantly");
        heroTitle.setTextFill(Color.WHITE);
        heroTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 42));

        Label heroSubtitle = new Label(
    "Convert currencies in a snap. 17 global currencies, " +
    "lightning-fast results, always accurate."
);
        heroSubtitle.setTextFill(Color.rgb(255, 255, 255, 0.9));
        heroSubtitle.setFont(Font.font("Segoe UI", 16));
        heroSubtitle.setWrapText(true);
        heroSubtitle.setMaxWidth(850);
        heroSubtitle.setAlignment(Pos.CENTER);

        VBox heroBox = new VBox(14, heroTitle, heroSubtitle);
        heroBox.setAlignment(Pos.CENTER);
        heroBox.setPadding(new Insets(20, 0, 0, 0));

        VBox.setVgrow(heroBox, Priority.ALWAYS);

        // ---------------- ASSEMBLE HEADER ----------------
        header.getChildren().addAll(topNav, heroBox);
        root.setTop(header);

        // Main area: left rates, center converter, right history
        HBox main = new HBox(24);
        main.setPadding(new Insets(24));
        main.setAlignment(Pos.TOP_CENTER);

        // Create cards for rates, converter, and history
        VBox ratesCard = createRatesCard();
        VBox converterCard = createConverterCard();
        VBox historyCard = createHistoryCard();

        main.getChildren().addAll(ratesCard, converterCard, historyCard);
        HBox.setHgrow(converterCard, Priority.ALWAYS);

        root.setCenter(main);

        // Load history data after UI is set up
        loadHistoryFromFile();
        // ================================================

        Scene scene = new Scene(root);

        primaryStage.setTitle("Currency Converter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Creates the top navigation bar with icon, title, and buttons for Help and About.
     */
    private HBox createTopNav(Stage stage) {
        Label icon = new Label("💱");
        icon.setFont(Font.font(35));
        icon.setTextFill(Color.WHITE);

        Label title = new Label("Currency Converter");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);

        HBox left = new HBox(10, icon, title);
        left.setAlignment(Pos.CENTER_LEFT);

        Button helpBtn = NavButton("❓", "Help");
        helpBtn.setOnAction(e -> showHelpBox(stage));

        Button aboutBtn = NavButton("ℹ", "About");
        aboutBtn.setOnAction(e -> showAboutBox(stage));

        for (Button b : new Button[]{helpBtn, aboutBtn}) {
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.WHITE);
            b.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        }

        HBox right = new HBox(16, helpBtn, aboutBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nav = new HBox(20, left, spacer, right);
        nav.setPadding(new Insets(16, 28, 0, 28));
        nav.setAlignment(Pos.CENTER);

        return nav;
    }

    /**
     * Creates a navigation button with icon and text, including hover effects.
     */
    private Button NavButton(String icon, String text) {
        Button btn = new Button(icon + "  " + text);
        
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setCursor(Cursor.HAND);
        btn.setPadding(new Insets(6, 16, 6, 16));

        // Default outline pill style
        btn.setBackground(new Background(
                new BackgroundFill(
                        Color.TRANSPARENT,
                        new CornerRadii(18),
                        Insets.EMPTY
                )
        ));

        btn.setBorder(new Border(
                new BorderStroke(
                        Color.rgb(255, 255, 255, 0.45),
                         BorderStrokeStyle.SOLID,
                         new CornerRadii(18),
                         new BorderWidths(1)
                )
         ));

        // Hover effect to fill the button
        Background hoverBg = new Background(
                new BackgroundFill(
                        Color.rgb(255, 255, 255, 0.18),
                        new CornerRadii(18),
                        Insets.EMPTY
                )
        );

        btn.setOnMouseEntered(e -> btn.setBackground(hoverBg));
        btn.setOnMouseExited(e -> btn.setBackground(
                new Background(
                        new BackgroundFill(
                        Color.TRANSPARENT,
                        new CornerRadii(18),
                        Insets.EMPTY
                        )
                )
        ));
        return btn;
    }
    
    /**
     * Creates an info card with a title and content for dialogs like Help and About.
     */
    private VBox createInfoCard(String titleText, Node content) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(26));
        card.setPrefWidth(560);
        
        card.setBackground(new Background(
                new BackgroundFill(
                        Color.web("#f2f2f2"),
                        CornerRadii.EMPTY,   
                        Insets.EMPTY
                )
        ));

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2b2b2b"));

        card.getChildren().addAll(title, content);
        return card;
    }

    /**
     * Displays the Help dialog with instructions on how to use the application.
     */
    private void showHelpBox(Stage owner) {
        VBox content = new VBox(10);

        Label text;
        text = new Label(
                """
                How to Use the Currency Converter
                
                1. ENTER AN AMOUNT
                Type the numeric value you want to convert in the Amount field.
                
                2. SELECT CURRENCIES
                \u2022 Choose the currency you are converting from.
                \u2022 Choose the currency you want to convert to.
                
                3. CONVERT
                Click the Convert button to instantly calculate the converted value.
                
                4. VIEW RESULT
                The converted amount will appear in the Result section.
                
                5. CONVERTION HISTORY
                \u2022 Your last conversions are saved automatically.
                \u2022 The most recent conversion appears at the top.
                
                6. CLEAR INPUT
                Click Clear to reset the amount and result fields.
                
                !! IMPORTANT NOTES !!
                \u2022 Only numeric values are accepted.
                \u2022 Exchange rates are sample rates and may not reflect real-time market values.
                \u2022 The application supports multiple international currencies.""");

        text.setWrapText(true);
        text.setFont(Font.font("Segoe UI", 14));
        text.setTextFill(Color.web("#555555"));

        Button gotIt = new Button("Got it");
        gotIt.setPrefHeight(42);
        gotIt.setMaxWidth(Double.MAX_VALUE);
        gotIt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gotIt.setTextFill(Color.WHITE);
        gotIt.setBackground(new Background(
                new BackgroundFill(Color.web("#8f39ff"), new CornerRadii(12), Insets.EMPTY)
        ));

        VBox.setMargin(gotIt, new Insets(12, 0, 0, 0));

        content.getChildren().addAll(text, gotIt);

        VBox card = createInfoCard("Help", content);

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(card);
        scene.setFill(Color.TRANSPARENT);

        dialog.setScene(scene);

        gotIt.setOnAction(e -> dialog.close());

        dialog.showAndWait();
    }

    /**
     * Displays the About dialog with information about the application and developers.
     */
    private void showAboutBox(Stage owner) {
        
        Label text;
        text = new Label(
                """
                            Currency Converter Application
                                             This Currency Converter is a desktop application designed to provide fast and easy currency conversion using a clean and user-friendly interface.
                                             Features:
                            \u2022 Conversion between multiple world currencies
                            \u2022 Clean and modern user interface
                            \u2022 Conversion history tracking
                            \u2022 Simple and intuitive controls
                                             Technology Used:
                            \u2022 Java (programming language)
                            \u2022 JavaFX (GUI framework)
                            \u2022 NetBeans (IDE)
                                             Purpose:
                            This application was developed for educational purposes to demonstrate Java GUI programming, event handling, and basic financial calculations.
                                             Version: 1.0
                            Developer: Caboverde, Chanice
                                               Domaog, Maira Lorraine
                                               Virtudes, Juvelyn
                            Year: 2025""");

        text.setWrapText(true);
        text.setFont(Font.font("Segoe UI", 14));
        text.setTextFill(Color.web("#555555"));

        VBox card = createInfoCard("About", text);

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // Add close button to the card
        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));
        closeBtn.setTextFill(Color.WHITE);
        closeBtn.setBackground(new Background(
            new BackgroundFill(Color.web("#8f39ff"), new CornerRadii(21), Insets.EMPTY)
        ));
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setPrefSize(42, 42);
        closeBtn.setMinSize(42, 42);
        closeBtn.setMaxSize(42, 42);
        closeBtn.setPadding(new Insets(0, 0, 3, 0));
        
        // Drop shadow for visibility
        DropShadow btnShadow = new DropShadow();
        btnShadow.setRadius(8);
        btnShadow.setOffsetY(2);
        btnShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        closeBtn.setEffect(btnShadow);
        
        // Hover effect
        closeBtn.setOnMouseEntered(e -> {
            closeBtn.setBackground(new Background(
                new BackgroundFill(Color.web("#7a2ed6"), new CornerRadii(20), Insets.EMPTY)
            ));
        });
        closeBtn.setOnMouseExited(e -> {
            closeBtn.setBackground(new Background(
                new BackgroundFill(Color.web("#8f39ff"), new CornerRadii(20), Insets.EMPTY)
            ));
        });
        
        closeBtn.setOnAction(e -> dialog.close());
        
        // Position close button in upper right
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(15, 15, 0, 0));
        
        StackPane wrapper = new StackPane(card, closeBtn);

        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        dialog.showAndWait();
    }

    /**
     * Creates a generic card VBox with shadow and white background, used as a base for other cards.
     */
    private VBox createCard(double width) {
        VBox card = new VBox(14);
        card.setPrefWidth(width);
        card.setPadding(new Insets(20));
        card.setBackground(new Background(
                new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)
        ));

        DropShadow shadow = new DropShadow();
        shadow.setRadius(18);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(0,0,0,0.12));
        card.setEffect(shadow);

        return card;
    }

    /**
     * Creates the rates card displaying currency rates relative to USD.
     * This card shows a list of currencies with their flags, codes, names, and rates.
     */
    private VBox createRatesCard() {
        // Create a card with a fixed width of 340 pixels
        VBox card = createCard(340);

        // Title label for the rates section
        Label title = new Label("Currency Rates");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        // Subtitle indicating rates are per 1 USD
        Label sub = new Label("(per 1 USD)");
        sub.setTextFill(Color.GRAY);

        // Header row with title and subtitle, using a spacer for alignment
        HBox headerRow = new HBox(title, new Region(), sub);
        HBox.setHgrow(headerRow.getChildren().get(1), Priority.ALWAYS);

        // Vertical box to hold the list of currency rows
        VBox list = new VBox(10);
        list.setPadding(new Insets(8));

        // Iterate over each currency entry in the rates map
        for (Map.Entry<String, Double> e : rates.entrySet()) {
            // Get the circular flag for the currency code
            StackPane flag = flagFor(e.getKey());
            flag.setTranslateX(-6);

            // Label for the currency code (e.g., "USD")
            Label code = new Label(e.getKey());
            code.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

            // Label for the full currency name
            Label name = new Label(currencyFullName(e.getKey()));
            name.setFont(Font.font("Segoe UI", 12));
            name.setTextFill(Color.GRAY);
            name.setMaxWidth(170);

            // Tooltip showing the full currency name on hover
            Tooltip tip = new Tooltip(currencyFullName(e.getKey()));
            tip.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
            tip.setWrapText(true);
            tip.setMaxWidth(260);
            tip.setStyle(
                "-fx-background-color: #eeeeee;" +
                "-fx-text-fill: #000000;" +
                "-fx-padding: 12;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: #cccccc;"
            );

            Tooltip.install(name, tip);

            // Vertical box containing code and name labels
            VBox textBox = new VBox(2, code, name);

            // Label for the exchange rate, formatted to 4 decimal places
            Label rate = new Label(String.format("%.4f", e.getValue()));
            rate.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            rate.setTextFill(Color.web("#8f39ff"));
            rate.setMinWidth(90);
            rate.setAlignment(Pos.CENTER_RIGHT);
            rate.setWrapText(false);

            // Spacer region to push the rate label to the right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Horizontal row containing flag, text box, spacer, and rate
            HBox row = new HBox(12, flag, textBox, spacer, rate);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPadding(new Insets(10, 8, 10, 8));
            row.setMinHeight(56);
            row.setPrefHeight(56);

            // Add the row to the list
            list.getChildren().add(row);
        }

        // ScrollPane to make the list scrollable if it exceeds the height
        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.setPrefHeight(450);
        sp.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" 
        );

        // Add header, separator, and scroll pane to the card
        card.getChildren().addAll(headerRow, new Separator(), sp);

        return card;
    }

    /**
     * Creates the converter card with input fields, currency selectors, and conversion logic.
     * This is the main interface for performing currency conversions.
     */
    private VBox createConverterCard() {
        // Create a card with a fixed width of 520 pixels
        VBox card = createCard(520);
        card.setSpacing(18);
        card.setAlignment(Pos.TOP_CENTER);

        // Title label for the converter section
        Label title = new Label("Convert Currency");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#6a2ccf"));
        title.setAlignment(Pos.CENTER);

        // Subtitle providing instructions
        Label subtitle = new Label("Enter an amount and select currencies");
        subtitle.setTextFill(Color.GRAY);
        subtitle.setAlignment(Pos.CENTER);

        // TextField for entering the amount to convert
        TextField amount = new TextField();
        amount.setPromptText("Amount");
        amount.setPrefHeight(46);
        amount.setFont(Font.font(14));
        amount.setMaxWidth(Double.MAX_VALUE);
        amount.setBackground(new Background(
                new BackgroundFill(Color.web("#f8f7fc"), new CornerRadii(10), Insets.EMPTY)
        ));

        // Labels for "From" and "To" currency selectors
        Label fromLabel = new Label("From");
        Label toLabel = new Label("To");

        // ComboBoxes for selecting currencies, with flags
        ComboBox<String> fromCombo = comboWithFlags();
        ComboBox<String> toCombo = comboWithFlags();
        fromCombo.getItems().addAll(rates.keySet());
        toCombo.getItems().addAll(rates.keySet());
        fromCombo.getSelectionModel().select("EUR");
        toCombo.getSelectionModel().select("USD");

        fromCombo.setPrefHeight(44);
        toCombo.setPrefHeight(44);
        fromCombo.setPrefWidth(200);
        toCombo.setPrefWidth(200);

        // Vertical boxes for "From" and "To" sections
        VBox fromBox = new VBox(6, fromLabel, fromCombo);
        VBox toBox = new VBox(6, toLabel, toCombo);

        // Swap button to exchange "From" and "To" currencies
        Button swapBtn = new Button("⇄");
        swapBtn.setPrefSize(42, 42);
        swapBtn.setFont(Font.font(15));
        swapBtn.setTextFill(Color.web("#8f39ff"));
        swapBtn.setBackground(new Background(
                new BackgroundFill(Color.web("#f2ecff"), new CornerRadii(21), Insets.EMPTY)
        ));

        swapBtn.setOnAction(e -> {
            int f = fromCombo.getSelectionModel().getSelectedIndex();
            int t = toCombo.getSelectionModel().getSelectedIndex();
            fromCombo.getSelectionModel().select(t);
            toCombo.getSelectionModel().select(f);
        });

        // Vertical box for the swap button
        VBox swapBox = new VBox(swapBtn);
        swapBox.setAlignment(Pos.CENTER);
        swapBox.setPadding(new Insets(18, 6, 0, 6));

        // Horizontal row containing From, Swap, and To sections
        HBox currencyRow = new HBox(14, fromBox, swapBox, toBox);
        currencyRow.setAlignment(Pos.CENTER_LEFT);

        // Convert button to perform the conversion
        Button convert = new Button("Convert");
        convert.setPrefHeight(50);
        convert.setMaxWidth(Double.MAX_VALUE);
        convert.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        convert.setTextFill(Color.WHITE);
        convert.setBackground(new Background(
                new BackgroundFill(Color.web("#8f39ff"), new CornerRadii(14), Insets.EMPTY)
        ));

        // Allow pressing Enter in the amount field to trigger conversion
        amount.setOnAction(e -> convert.fire());

        // Clear button to reset inputs and result
        Button clear = new Button("Clear");
        clear.setPrefHeight(50);
        clear.setPrefWidth(100);
        clear.setTextFill(Color.web("#800000")); 
        clear.setBackground(new Background(
                new BackgroundFill(Color.TRANSPARENT, new CornerRadii(14), Insets.EMPTY)
        ));
        clear.setBorder(new Border(new BorderStroke(
                Color.web("#7A1F2B"), BorderStrokeStyle.SOLID,
                new CornerRadii(14), new BorderWidths(1)
        )));

        // Horizontal row for Convert and Clear buttons
        HBox actionRow = new HBox(12, convert, clear);
        HBox.setHgrow(convert, Priority.ALWAYS);

        // Label for the result section
        Label resultTitle = new Label("Result");

        // Label to display the conversion result
        Label resultLabel = new Label();
        resultLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        resultLabel.setTextFill(Color.web("#6a2ccf"));
        resultLabel.setWrapText(true);
        resultLabel.setAlignment(Pos.CENTER_LEFT);
        resultLabel.setMinHeight(60);
        resultLabel.setPadding(new Insets(12));

        // Event handler for the Convert button
        convert.setOnAction(e -> {
            try {
                String amountText = amount.getText().trim();
                if (amountText.isEmpty()) {
                    resultLabel.setText("Please enter an amount.");
                    logError("EMPTY_INPUT", "User attempted conversion with empty amount");
                    return;
                }
                
                // Remove commas and spaces, but keep the decimal point (period)
                String cleanAmount = amountText.replaceAll(",", "").replaceAll("\\s", "");
                
                // Check if the cleaned string is valid
                if (cleanAmount.isEmpty()) {
                    resultLabel.setText("Please enter an amount.");
                    return;
                }
                
                double amt = Double.parseDouble(cleanAmount);
                
                if (amt <= 0) {
                    resultLabel.setText("Amount must be greater than zero.");
                    logError("INVALID_AMOUNT", "User entered non-positive amount: " + amt);
                    return;
                }
                
                String from = fromCombo.getValue();
                String to = toCombo.getValue();
                
                if (from == null || to == null) {
                    resultLabel.setText("Please select both currencies.");
                    logError("MISSING_CURRENCY", "Currency selection incomplete");
                    return;
                }
                
                double converted = convert(from, to, amt);

                resultLabel.setText(
                        df.format(amt) + " " + from + " = " +
                        df.format(converted) + " " + to
                );

                addHistoryEntry(amt, from, to, converted);
                logUserBehavior(from, to);
                
                if (amt > PRECISION_THRESHOLD || isVolatileCurrency(from, to)) {
                    savePrecisionConversion(amt, from, to, converted);
                }
                
            } catch (NumberFormatException ex) {
                resultLabel.setText("Invalid amount. Please enter a valid number.");
                logError("PARSE_ERROR", "User entered invalid number format: " + amount.getText());
            } catch (Exception ex) {
                resultLabel.setText("An error occurred during conversion.");
                logError("CONVERSION_ERROR", "Unexpected error: " + ex.getMessage());
            }
        });

        // Event handler for the Clear button
        clear.setOnAction(e -> {
            amount.clear();
            resultLabel.setText("");
        });

        // Add all components to the card
        card.getChildren().addAll(
                title, subtitle,
                amount,
                currencyRow,
                actionRow,
                resultTitle, resultLabel
        );

        return card;
    }

private VBox createHistoryCard() {
        // Creates the main container for the history panel, often a stylized card.
        VBox card = createCard(350);
        card.setSpacing(12);

        // Title label for the history section
        Label title = new Label("Conversion History");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        // VBox to hold individual history entries. This is the content that scrolls.
        VBox historyList = new VBox(10);
        historyList.setPadding(new Insets(8));

        // Store reference for history updates so other methods can add new entries.
        this.historyContainer = historyList;

        // ScrollPane wraps the historyList to enable scrolling when content overflows.
        ScrollPane scroll = new ScrollPane(historyList);
        scroll.setPrefHeight(420);
        scroll.setStyle(
            "-fx-background-color: transparent;" + // Makes the scroll pane background transparent
            "-fx-border-color: transparent;"       // Removes the default scroll pane border
        );

        // Scrollbar policies
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Button for clearing the history
        Button clearBtn = new Button("Clear History");
        // Margin for spacing the button from the scroll pane
        VBox.setMargin(clearBtn, new Insets(12, 0, 0, 0));
        clearBtn.setPrefHeight(36);
        clearBtn.setMaxWidth(Double.MAX_VALUE); // Makes button stretch to card width

        // Styling for the clear button (red text, transparent background, red border)
        clearBtn.setTextFill(Color.web("#800000"));
        clearBtn.setBackground(new Background(
                new BackgroundFill(
                        Color.TRANSPARENT,
                        new CornerRadii(8),
                        Insets.EMPTY
                )
        ));
        clearBtn.setBorder(new Border(new BorderStroke(
                Color.web("#7A1F2B"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));

        // ===== CLEAR BOTH UI AND FILE =====
        // Action to clear the history: clear UI children and delete file contents.
        clearBtn.setOnAction(e -> {
            historyList.getChildren().clear();
            clearHistoryFile();
        });
        // ==================================

        // Add all components to the main card VBox
        card.getChildren().addAll(title, scroll, clearBtn);

        return card;
    }

    private void addHistoryEntry(double amount, String from, String to, double converted) {
        // Creates a horizontal box for a single history entry row.
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(10));

        // Styling for the row: light gray border with rounded corners.
        row.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));

        // Label for the original amount and currency (left side)
        Label left = new Label(df.format(amount) + " " + from);
        left.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        left.setTextFill(Color.web("#8565c4")); // Purple color

        // Arrow label for visual separation
        Label arrow = new Label("   --->   ");
        arrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        arrow.setTextFill(Color.GRAY);

        // Label for the converted amount and currency (right side)
        Label right = new Label(df.format(converted) + " " + to);
        right.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        right.setTextFill(Color.web("#6a3df0")); // Darker purple color

        // Add the labels to the row
        row.getChildren().addAll(left, arrow, right);

        // Add the new history entry to the UI at index 0 (top/newest first).
        historyContainer.getChildren().add(0, row);
        
        // Persist the history entry to the file.
        saveHistoryToFile(amount, from, to, converted);
    }

    // ============== ENHANCED FILE HANDLING METHODS ==============
    
    private void loadHistoryFromFile() {
        // Defines the file to load history from.
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            // If the file doesn't exist, there is no history to load.
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            java.util.List<String> lines = new java.util.ArrayList<>();
            
            // Read all lines first from the file.
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            
            // Add them in reverse order (newest first in the UI).
            for (int i = lines.size() - 1; i >= 0; i--) {
                // Split the line by comma to get conversion parts (amount, from, to, converted)
                String[] parts = lines.get(i).split(",");
                if (parts.length == 4) {
                    try {
                        // Parse values and trim whitespace
                        double amt = Double.parseDouble(parts[0].trim());
                        String from = parts[1].trim();
                        String to = parts[2].trim();
                        double conv = Double.parseDouble(parts[3].trim());
                        
                        // Creates and adds the UI element for this history entry.
                        addHistoryEntryToUI(amt, from, to, conv);
                    } catch (NumberFormatException e) {
                        // Log error if a line is malformed (e.g., non-numeric where a number is expected)
                        logError("LOAD_HISTORY_PARSE", "Invalid history line: " + lines.get(i));
                    }
                }
            }
        } catch (IOException e) {
            // Log error if there's an issue with reading the file (e.g., permissions)
            logError("LOAD_HISTORY_IO", "Error loading history: " + e.getMessage());
        }
    }

    private void saveHistoryToFile(double amount, String from, String to, double converted) {
        // Appends the new conversion entry to the history file.
        // FileWriter(HISTORY_FILE, true) enables append mode.
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            
            // Format the entry as a comma-separated line.
            pw.println(amount + "," + from + "," + to + "," + converted);
            
        } catch (IOException e) {
            // Log error if saving fails.
            logError("SAVE_HISTORY", "Error saving history: " + e.getMessage());
        }
    }

    private void clearHistoryFile() {
        // Clears the contents of the history file by opening and immediately closing
        // a PrintWriter without writing anything (overwrites the file).
        try {
            File file = new File(HISTORY_FILE);
            if (file.exists()) {
                // Overwrites the file content with an empty string, effectively clearing it.
                new PrintWriter(file).close();
            }
        } catch (IOException e) {
            // Log error if file clearing fails.
            logError("CLEAR_HISTORY", "Error clearing history file: " + e.getMessage());
        }
    }

    private void addHistoryEntryToUI(double amount, String from, String to, double converted) {
        // This is a helper method, similar to the first part of addHistoryEntry, 
        // specifically for loading entries from the file and creating the UI row.
        
        // Creates a horizontal box for a single history entry row.
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(10));

        // Styling for the row: light gray border with rounded corners.
        row.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));

        // Label for the original amount and currency (left side)
        Label left = new Label(df.format(amount) + " " + from);
        left.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        left.setTextFill(Color.web("#8565c4"));

        // Arrow label for visual separation
        Label arrow = new Label("   --->   ");
        arrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        arrow.setTextFill(Color.GRAY);

        // Label for the converted amount and currency (right side)
        Label right = new Label(df.format(converted) + " " + to);
        right.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        right.setTextFill(Color.web("#6a3df0"));

        // Add the labels to the row
        row.getChildren().addAll(left, arrow, right);

        // Add the loaded history entry to the UI (at the end for loading, since lines were read in reverse).
        historyContainer.getChildren().add(row);
    }
    
    // ============== ERROR LOGGING ==============
    
    private void logError(String errorType, String details) {
        // Appends a timestamped error message to a dedicated log file.
        // FileWriter(ERROR_LOG_FILE, true) enables append mode.
        try (FileWriter fw = new FileWriter(ERROR_LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            
            // Get current timestamp and format it.
            String timestamp = LocalDateTime.now().format(timeFormatter);
            // Write the log entry: Timestamp | Error Type | Details
            pw.println(timestamp + " | " + errorType + " | " + details);
            
        } catch (IOException e) {
            // If logging fails, fall back to console output.
            System.err.println("Error logging error: " + e.getMessage());
        }
    }
    
    // ============== USER BEHAVIOR TRACKING ==============
    
    private void logUserBehavior(String from, String to) {
        // Tracks the usage frequency of specific currency pairs.
        String pair = from + "->" + to;
        // Increment the count for the currency pair.
        currencyPairUsage.put(pair, currencyPairUsage.getOrDefault(pair, 0) + 1);
        
        // Write the updated usage statistics to a file (overwrites the file).
        // FileWriter(USER_BEHAVIOR_FILE, false) enables overwrite mode.
        try (FileWriter fw = new FileWriter(USER_BEHAVIOR_FILE, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            
            // Write header and update time
            pw.println("=== Currency Pair Usage Statistics ===");
            pw.println("Updated: " + LocalDateTime.now().format(timeFormatter));
            pw.println();
            
            // Stream, sort (by usage count descending), and write each pair's statistics.
            currencyPairUsage.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort descending by count
                .forEach(entry -> pw.println(entry.getKey() + ": " + entry.getValue() + " conversions"));
            
        } catch (IOException e) {
            // Log error if saving user behavior fails.
            logError("USER_BEHAVIOR", "Error saving user behavior: " + e.getMessage());
        }
    } 

private void loadUserBehavior() {
        // Defines the file containing user behavior statistics (currency pair usage counts).
        File file = new File(USER_BEHAVIOR_FILE);
        if (!file.exists()) {
            // If the file doesn't exist, no behavior data to load.
            return;
        }
        
        // Use try-with-resources for automatic resource closing.
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Read the file line by line.
            while ((line = br.readLine()) != null) {
                // Check if the line contains a currency pair separator "->" and a count separator ":".
                // This skips header lines and empty lines.
                if (line.contains("->") && line.contains(":")) {
                    // Split the line into the currency pair and the count part.
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String pair = parts[0].trim();
                        try {
                            // Extract the count by trimming the right side and splitting off " conversions".
                            int count = Integer.parseInt(parts[1].trim().split(" ")[0]);
                            // Load the pair and its count into the map (currencyPairUsage).
                            currencyPairUsage.put(pair, count);
                        } catch (NumberFormatException e) {
                            // Skip invalid lines where the count is not an integer.
                            // The error is swallowed here to tolerate malformed log entries without crashing.
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Log error if there is a file reading issue.
            logError("LOAD_BEHAVIOR", "Error loading user behavior: " + e.getMessage());
        }
    }
    
    // ============== PRECISION LOGGING ==============
    
    private boolean isVolatileCurrency(String from, String to) {
        // Defines a list of currency codes considered "volatile" for logging purposes.
        String[] volatileCurrencies = {"TRY", "MXN", "PHP", "KRW", "ARS", "VES"};
        
        // Check if either the source or target currency is in the volatile list.
        for (String volatileCurrency : volatileCurrencies) {
            if (from.equals(volatileCurrency) || to.equals(volatileCurrency)) {
                return true;
            }
        }
        return false;
    }
    
    private void savePrecisionConversion(double amount, String from, String to, double converted) {
        // Logs conversions that meet specific criteria (large amount or volatile currency) 
        // with higher precision (10 decimal places).
        try (FileWriter fw = new FileWriter(PRECISION_LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            
            String timestamp = LocalDateTime.now().format(timeFormatter);
            // Determine the reason for precision logging. (Assumes a variable PRECISION_THRESHOLD exists)
            String reason = amount > PRECISION_THRESHOLD ? "LARGE_AMOUNT" : "VOLATILE_CURRENCY";
            
            // Format the log entry with high precision (%.10f) and the reason.
            pw.println(String.format(
                "%s | %s | %.10f %s -> %.10f %s | Reason: %s",
                timestamp, reason, amount, from, converted, to, reason
            ));
            
        } catch (IOException e) {
            // Log error if saving the precision log fails.
            logError("PRECISION_LOG", "Error saving precision conversion: " + e.getMessage());
        }
    }

    // ============== END FILE HANDLING ==============

    private double convert(String from, String to, double amount) {
        // Core conversion logic using USD as the base currency.
        // (Assumes a Map<String, Double> rates contains exchange rates relative to USD).
        double fromRate = rates.get(from);
        double toRate = rates.get(to);
        
        // 1. Convert the input amount to USD: amount / rate_from
        double amountInUSD = amount / fromRate;
        
        // 2. Convert the USD amount to the target currency: amount_in_usd * rate_to
        return amountInUSD * toRate;
    }

    private String currencyFullName(String code) {
        // Provides the full name for a given currency code.
        return switch (code) {
            case "USD" -> "US Dollar";
            case "EUR" -> "Euro";
            case "GBP" -> "British Pound";
            case "JPY" -> "Japanese Yen";
            case "CAD" -> "Canadian Dollar";
            case "AUD" -> "Australian Dollar";
            case "CHF" -> "Swiss Franc";
            case "CNY" -> "Chinese Yuan";
            case "SEK" -> "Swedish Krona";
            case "NZD" -> "New Zealand Dollar";
            case "MXN" -> "Mexican Peso";
            case "SGD" -> "Singapore Dollar";
            case "HKD" -> "Hong Kong Dollar";
            case "NOK" -> "Norwegian Krone";
            case "KRW" -> "South Korean Won";
            case "TRY" -> "Turkish Lira";
            case "PHP" -> "Philippine Peso";
            default -> code;
        }; // Return the code itself if the full name is not found.
    }

    private StackPane flagFor(String code) {
        // Creates a circular flag graphic for a given currency code.
        try {
            // Retrieve the flag image data (assumes getFlagBase64 exists).
            String base64Image = getFlagBase64(code);
            // Convert the Base64 string into a JavaFX Image object.
            Image img = imageFromBase64(base64Image);
        
            // Return circular flag with 34px size
            return circularFlag(img, 34);
        
        } catch (Exception ex) {
            // Fallback to empty circular placeholder if flag data cannot be loaded.
            Canvas canvas = new Canvas(34, 34);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.LIGHTGRAY);
            gc.fillOval(0, 0, 34, 34); // Draw a gray circle
        
            // Take a snapshot of the canvas to create a WritableImage
            WritableImage fallbackImg = new WritableImage(34, 34);
            canvas.snapshot(null, fallbackImg);
        
            // Wrap the fallback image in the circular container
            return circularFlag(fallbackImg, 34);
        }
    }

private StackPane circularFlag(Image img, double size) {
    // Helper method to create a circular image view.
    ImageView iv = new ImageView(img);
    // Set fixed size and disable ratio preservation.
    iv.setFitWidth(size);
    iv.setFitHeight(size);
    iv.setPreserveRatio(false);
    
    // Wrapper StackPane to hold the ImageView.
    StackPane wrapper = new StackPane(iv);
    // Ensure the wrapper has the exact required size.
    wrapper.setMinSize(size, size);
    wrapper.setPrefSize(size, size);
    wrapper.setMaxSize(size, size);
    
    // Create a circular clip shape. 
    Circle clip = new Circle(size / 2);
    // Bind the center of the clip to the center of the wrapper.
    clip.centerXProperty().bind(wrapper.widthProperty().divide(2));
    clip.centerYProperty().bind(wrapper.heightProperty().divide(2));
    // Apply the clip to the wrapper.
    wrapper.setClip(clip);
    
    return wrapper;
}

    private Image imageFromBase64(String dataUrl) {
        // Converts a Base64 encoded image string (potentially with a data URL prefix) to a JavaFX Image.
        
        // Strip any leading data URL prefixes (handles duplicated prefixes)
        String base64 = dataUrl;
        // Loop to strip common data URL patterns like "data:image/png;base64,"
        while (base64.toLowerCase().startsWith("data:image")) {
            int comma = base64.indexOf(',');
            if (comma < 0) break;
            base64 = base64.substring(comma + 1);
        }
        // Decode the Base64 string into a byte array.
        byte[] bytes = Base64.getDecoder().decode(base64);
        // Create a new JavaFX Image from the byte array stream.
        return new Image(new ByteArrayInputStream(bytes));
    }

    private String getFlagBase64(String code) {
        return switch (code.toUpperCase()) {
            case "USD" -> "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAADvQAAA70BLvRiPgAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAByKSURBVHic7Z15XFVl/sc/zznnLsBlFbyArMoiKi5AKCqay8xYv2bRmqkprUZLc2kvnZqc0qYps2aayl7TTJPTpFPqmKWpmKbllpigIIsIKgjKJiByL9zlnPP8/kCuXLjcfVN4/wXnPOc533Of73me7/N9vs/3EEopbjXuy3g+NESmvV0jchM6wI0SCYkQKBOiBxegIZxcDSk7gtZpVws5zQAAQkVKcYGnpF0QiK9A6WUCVFLgAiG4QImkUqvFxRFFRToPP5rT4TwtgKMsz3ghUJA0P9oKn7tbiF9yHfwDmqXRLCgA0q0gMb6OgviCUF/DaYIYCaGQMBQALmh5cicvkkBKAVAeMgnEc2nDLxFKLgDiEZEwe1oZv6PpJ07o3fCYLoPcbD0A+c0Wdv6lo7+kInm4gSiyqhASyoOxuZ7J9DxeFXeZLSOIpEUrkGAzP1EbgG9BSQ5LsCfuZGmlzYJ4mJtGAd6acP+iM0S5qIiEp7bCx+GeyxoFAAAK6HUCkfACsVgWQBmAHIjYPqzgzH5HZXQHXq0Av5q4MjmEqtdUkeBZlxEos/X60BAfXGnuMHnOWgXoQscT6EWrlOA6tBhg3pX5qD6NOlptWggvwCsVYMHkp3/SIvi8c5YoRwg9B28rGRzqi4fuHY21646ZPG+rAgCAViCwsifoThMB/QdPsS7pVNklWy92NbYPni5kxcSFj82e+IfLR8X4b0pJuN2NDwDTJsfi9onRYBj76+iJjKXgWJtfmEEU5AWWkMpzY1M+Kx+TNN5pAjkBr+gB/rd2w+rI8WNeOH1BxQFA9aVr2LbrLETRNtmyMoZgfHokAGDSbUMQFRmAXfvOoU2tg0qlw8atxejQ8ADs6wG60PAMBNGuSzshOCRS8YnEk2dPOVCLU/CoArz39Hvp6T+ffGBEWrx/17Hd357D2nW5hoayBUKA384eicceGguJhDUcP13aiJffPITaepXhmCMKIFKgQ+9w5ymAYh2VCysTjpVfc7Qye/HIEDBq1Crp77Ke3bg+V/djCy8xNP6PJ2ux+u0jZhuf4/oWmVLgv18U4+PPCg3H2lQ6LF6+x6jxHYUhgIxz+MVhQfAE0bBl58el3O8MuezB7QqwNOvxewYFcC3HSOz9ek5GRo8YjLKKJhSWNGJUShikEvMizb1npMV7pKWGo6mlA/sPV8FfIUXi0GBniW+AYyhYZ/x6BOEUdOO5ccP3l6cnpTihRptwmyfwu+8q5Zve3bz1IEm4QwQhADAuVYmdeyvwwb9PQhQpFs4bi4yxETj6o2ljmWUZ3PerEThy/BLKzzebLBPgL4NGy2Pekh1oadVg1vShyBwXgTPlTUblNJxPK/EJqrTrYSiViSCxUqlM0IpsJW1vF+yqx5gQhmH+U/XCM5v1C5a8m5CQoHVCnRZxiw3w2da8WUVnrmz97uhF3+aWG1NiXx8J2juMPak+cs5oCAgKlGNobBAAIC46EM8vHY+vcs7im+8qAQCNTe2ovnRjCJXLOWi1PLo/Vs86AWDqxFi8teonDj+bTqeFTuv0tjrOcdzs0NDQy86uuCcu7wE2bsn7cNL4IQsv16nQ2qoxOtez8QH0aqhrbVqMTA7FwnljDeP/L2cl4ZezkrDv+wtY877xPF9jwn6wx6C0FolE6goFyOR5/sf6+vo5SqUy19mVd8elNsC2nYX5s2YMXfjHNYfw0cYCCDZO6wBAFCk+3VKEx5bvMXqr//Gfk1i55hBUas+uxRBCIJFKXVF1JKX0+7q6ugddUXkXLlGAnJyKgP0Hy5rHjBw8jmUIOAvWUoRSgYR484YaxxIQAjRf7exFuk/zTMEwBJMyo2wT3E6kUhkIcZ7DqRsyAJ/U1dW9DcD8A9uJ0xXg7Q8OxEr9hMa9318I/uWD/8OGLUW4fWKM2WtmZMdi2qRYs2WmZ8dh05elmP3QVrz216PITIs0Wz41JQy//sVwm+W3B0IIJBKX9AJdPFNfX7+rsrIyyNkVO9UGeG3tvpQj+ZdP7v2uSlpV02mYrVufj8ShIWavmzY5Fj5yDv/c0Ldj7Os95Si/0NL5994KFJQ0QCZjodWaNsBnZMchfXQ4/BVStKlcH8fBSSTQ6VxnuFNKfyqXy4/U1tZOjYiIuOKsep02C3h+5Y74U6VNxTWXr/lYvCkBHrh7JBbcPwYMSyC93p3r9AKoCKz/vBCfbimy2RU8KTMKLz45EQqFBBzLgGEI9HoBFMD+w1VY+36uwfB01iygOx3tagiCM2aEZsmTSqXTQ0JCnOI9dMoQ8OQLOyILzzSdtqbxgU6P3Yb/FeOplftwtfXGW6NS6fHMy9/ik02nbW58ADhyvAYLnt6J8nPNhkUgCuCdD3/EqrWHTc46nImLh4Eu0nU63dc1NTVW/daWcFgBHl62OazsfHPJxUvX/Hqei4rwN3WJgYLiBlyuazP8X9ugQn5hndlrLNVZ16DGydP1hv8pBXZ9e87sNc6Ck0hcZQz2JJvjuK0AJI5W5JAC3LVwh299o6a0qro1sOc5iYTFs0syzV4fEiRHSlIo/vbPE3jz/WNIiAvG4FBfs9c8szgTMpl5g/j2STH4Kucsnvnjt2hT6TAxwz2zAQBgWbc5V++oq6vbCAfb0BFpSTCnKTxRfXWQqZMZY8IxPm0Iwgb5orGp3WQFsdGBePSZ3Qa37qmiBsREBaLhiunywYFyZKZFIitjCL47ctFkmcFhfli3Pt9wfu6S7RhvYcbgTCQSCXjebb6JX9fW1l6LiIh4FJ2jnc3YbQQ+MX35rr2aqDu6H0scGoK594wEQ4Bh8cGIjwlCYUkjGhpV6NAK+GhjARoa1TbdJyTYB4/OHQOFrwShg3wxdpQSVTXXUH6uCRQEn28rQclZ243iNJ+rTe9EF5TYfKEJRK0uDgRSSCRyKlJ0aPkSsJzr3I89uda6O37n/tftudQuBVg+YeFLO5mRr4omInbGpSqxank2wgbd6MorLrRg5RsHUVndao+MGBLuj1UrsjEyOdRwrPmqBqvfOozcfPvc5Y7EA1hCyxPwNsUPOgyllNyRcKp0j60X2jx+rPzVyhn7maTVphofAE6ersfrfztqdGzFqwfMNj7D3JgKmuJSXRuee2W/kSv4nQ+PW2x8mdQlzjOLMMTtQTaEEPppVcaICFsvtEkBXhn1G2n41Ak7ZcEBZtV7QkYUWlo1+HRLEXR6ERPSzY/BY0cpkZlmXvYJ6ZHgeQEbthShqaUDWRlDzJZX+EnwgBWxA66Adc9MoCdhvChuACE2talNhesU4bvHZiXJpmb17dolBAhQSDFvyQ58sD4f85/8GnHR5j2Y1riCE4cG45FndmPd+nzMW7IDLMuYjQ6aPD4aP7093vwDuQiG8VCYHcX0c2OSX7TlEqttgC3Pvf3iCb/hr/3+qUkoOXsFn2/rtJ8am9pRUNxgKMcyBBQwcuRIJCz0+hseMkKAibdFwUfeOQl5etFtkEhYvHl9aVerE3DkeI3FOliWAc/fiM5MTQmDMqzTHXHPz4djzMjBWLsuF9fatAAhOJJb7ZSgUGvwgB3QhcCIdFp8QdkhawpbpQCvjLpX8bO///FayqghRk908Fg1/vzOUbRes90HHhMVgNXLs5GcYDyLrLjQgpVrDqLyou0Go8JPit8/MQEzsuOMjjdf1eBPfzmCH07ciDRytQLoRQId7xEFAEArtHrJSGs2s1o1BFT5R3357GtHSNdSLAB8sfMsVqw+YFfjA8DFmmt49NkcI69dcdkVLHhql12NDwAqtQ4vvX7Q0Dt1HZu3dIdR47sD1lNtDwAgCTKp8Lg1JS0qwNPjl47LQ9QMjmMQHCjHpbo26PQCIpS9PL+9mDnV/Bis1wuIDFfgWpsWzS0diBjsB95CwL2lOgEgLNQXgiCi+tI1KPyk8JG5fxM0Q6gD21qcAKUrK9ISwywVs6gAdaxik56wBvfqA4u3Y8FTuxAa4gOFX9+LHyHBcjw+Pw3mDOIRSaG4VNuGeUt3YN6yr3H2fDNSU8zL/NhDYxE+uG/lk0lZxEYFYtFzOZi7ZAc2bz+D2yeZj0dwFR4zBjsJJGBftVTIrA3wUtYj935BUj+nIBgS7o9L3RZuZFIWfn4SNLfcGBY4jkHXTqxfzErCs4szsXj5HpSUNQIABBEQur3h4YP90HCl3WDsEQKED1YYxfCzDAF7vT9NHBqCj/56J97/Vx62bC8F0LlJo7shGBQoh04nGK38RYYrcLnOeF+Aq20AwNN2AABAYBikxeedKeyrgFkFmJ31h+YzRGl1UH1cTCBWr5iCRBPhXcfzL2PV24eNFMYaAgNkePHJLEwxMfWsrG7FyjcOouJ6oIgtuEMBRErQofeoAgAE+4fln5nR5+m+FOD3r+5Z8dXusjdsvZ9UwuKpRbdh9p1JhmPrPs7Dxq3FcCT2ZPadSXh+6QTDkJJz4DzeePeHPiOCLJGdHsm//YfJpledHKOrxRUgBCpVW5tDD+4MgRT+vwoPDz9g6lyf1tGZ8qbnTR0fFheE81VX+3wmnV5AcVmjkQKcPF1v9jcI8JdBLmP7XAUEgJNF9Ub2xJnyJouNnxgfbAgj6wkjk3DEPyDAbAVOgAEJoNSRnaRO4XkAJhXApBH48hvfzCmruGJymffuu4ZjRLJ5Q23apFjs2ncO9y38EvmFdZg22byXb2pWtEVP4MzsOBQUN+C3i77Cl7vPWiwfFxOIB+9NNVvGHbBO2T/mMLPq6upM+sVN9gAXqtvWmDrOMARTs2Kgbtej+EyjyTuxLIOde8/hwJEqAMDjL+7F9B6OmZ5Mz46Fj1yCTV+V9lnmwsVWrP+sEIJIsea9Y5gyIdpsUOjM7DhMvG0IpBIWOr3L4/T6xEbXvKsgAJ4FML/XiZ42wJq/Hkzd+OXpwi63q1zOYdn8dAyNDYJUymJkcihUah3Kz7eAUootO870GZzRpzQEuH/OSEPc/ugRYSCEGFzKx/IuYcP/iu0KCv3tnBFgCMGwuCAE+MtQWt4EjYZHZXUr3v9XnkuDQk2h1+ug1dhm+LoIHcdxcaGhobXdD/ZSz+raq+u6+9w1Gh7vfXQCVTWthvV4hZ8UQ2OD8Nm2EpsbH+iM09u4tRjf/3ARo4aHgr0ewZuaEoajJy7ZFREMdAaFbthShNjoQAT4d6YUSkkchMt1Krz70QmXB4WagmE8syRtAqler1/W86CRAhACUlDSkNWzkFYn4M33j6H7xs4vdp3F4dwas3dU+JmPWdz0ZSly828oZH5hHTZsKTJrMFqq81jeZWzuNpS0qXR4/W9HTe4ZdAeMZ5aGTUIImYseGRONFOClN/YtuNLcbtIuGJkchsBAOT7dUoSC4gZMs7DbhxBg6fx0s2Xkcg63jYvAvoOV2Ln3HMalKi028JLfpYO1kPcne0I0zpQ3Yf1nhfCRcxg7Smm2vCshDOOuSGFriKmvrzfKUWTU2PX1qsV9XZmSNAhLV+xBQXEDWIbgoftSERMVgIs1pvcnjEgKxf/NHIb3/5UHdbvprnfsyMF4+4Nc7PimAgCQm38J41LDcehYtcnyMhmLO2cMw76DlX2Gj4cN8sXJwjp8+Okp8LyII8drkDpiMPIshJu7EkIIvCEX03V+A8CwpdpgBJaUQJrz/cGOa9e0jJ4XsOvb80bdJsOQXuNyz2OJ8cFIGxMOALhtbAQmZUZh266zqKppBShw4OhFo6BQa+oMCfbBT6bEAQSIjgzA3Xcl41jeJRzL6wwHO1XUgLKKJpvqBNxnBAJAR0c7BN4zQ5AJasLDw2NwPYrYMASUXyx6bf5vRzP3/DwZpWebeo2ZpoyynscqKlvAsQyWzU83WPiz70zCIw+MRdNVTa+IYGvqbG7pQE1tGx6+NxV335UMAJiQPgSPP5IBXx9Jr0wh1tTpbljGK6aCXUQ1NNyw8ww9wO4dJ6qTU6Oifr1gm1lrmRBY9GzOnBKHV38/xfD/vKU7LPrrLdUboVRg68dzDN7ANe8dw5e7z9pdZ0a0pOXdOf7lZiuwGwra0RFMWU4BjvPnQVt1HTqvSRJJRH571COLXwO62QCEihEhQXIEBsjMKsBPb4/HwR+qzWbdiI0OhCBSFBY3YFyqEnExQWYVYFhcEPx8JSgsMe1cAjrTwxACnCqqR2pKGOKie21GMkIiYTFrWrzBvuiJ/GJZsPYvu8xvXXISgkgUOp6Yj2J1IwSQ4roCMADwXua9yhVrc9lPNp3GlKxosxf/bNowi4kXRg0Pw+MvfIMlK/Zg1drDyBxnPuJ3RnacRdfu5PFR+PM7R7F4+R4sei4HyQnmt5xnjAnHL2Ylmi3TX6FAatnw4f7A9R6gng2YpxMo/v7JSQwKNt50KpUwGBTSuclDJmWRMUYJvZ5HcVnnbhyeF422frEsg1VvHcbV6/mAcg6cx4+njJxPIARQhvkZpkfTs2Mhk3LYvP1Mp4CUGsUJAMDH/y1E03U/RHHZFTy/aj9kUhZa3Q2nVWiIjyFzyJ0zh2FkchiSEwZ1BoWi057oXt5dENLz4wUeh5X40vEA9nEA0ASfO7vONLUYJ7bmBYqfTYvHIw+MMSxsTMmKwZSsGNRcvoaX3zxspACCIBoav686KQVSEkPxwpNZ8FfciCr6Yv0cqNR6rHnvB+w7WGm2DlO5gUKCfbB6RTZio24MD/9+9/8giBSffH4aH/+3oO+fxIV4kR+gOxMB7GMAoB7+o/sqJYoU//78NBY9lwOV+kaQ6eHcGjy47Gu79uUBwIEjVZi7dAfOVV41HKusbsWDy3b0anxrOXuuGQ89/rVhIQrozES2dMUe/HPDKbuSVDkF7/EB3ICSScB1G6CaBJkfUAFU1bRCJmUN4Vcqtc7h9GsNjWqIlIJSQBApCIHDKV21WsGQdILnRchkHKr6cFa5C0K8bAAAQIFMAGD+knlfkgoyi/JNyozCucqruH/xdqx++wjSRoeb3c9nDVER/ggOlOOJP+zFkuU5kMs4xMc4lgeJYQgmpEdi7bpc/PqRbSgpu4LsCeYN235KUEVaYhh5ecJDv9tE0j62VDptdDgKSxoMPUBUhD8ohVGgqK0kJwxCfaPaYDMo/KSIjvRHaY+0rrYQNsgXCj8pLlzsHFpYhmDsKGUvV7A7YgK7065nvG4koAyTxbVDan7F5jo9fe81tfY3fBfdXbhA57DiSOMDnVvVjIxSkXp0HaALhgCClykAI4rDGDWVuj1Ddb/E215/AABNYFREbt4DM4BT8MaZIKVkGLfg3lGFc9s7vParVq4iSDZcLwtNcV2sliBKRK0mEASAzCeCMGydrr2jb1+3J/DxE0ibSp0HIM3TstzqaLUa6HVe9+XZfAYglnd5DnCr4scAdEAB+i9+DACFp6UYwDMQQhQMgIEeoJ9CKfVjYGeGyQFuCSgDwHkf1BvgZkM1oAD9mwEF6OeoGDKgAP0ZFScCzQRwy1cq+yuUUhkFeACe26dummZONX1sM+n8PNkALkTLE44Xifvz1ZmBAs0MocR04PwAtz6UVDAUoot2xwzQHZF64XowaDnDMBjoAforLFPB8JQb6AHcAPVChyvP0XJmSG5pE0Bsz7Q4gG143xDQMjy3tOn6vmWa71lZbm0o9cIFF4p8wJAfgBz2pCy3PF4YEEhJZ5szAEAorPq6xAD24UXpYQwwRDwEdPUAWvkxdHqqBnAB1PvGf14lyI8B1xVAWVCgBmjf324f4Fbj1OiCAjXQLUMImzLmS6rTDKwJOAsCRhToSAAgMp9Wpq3d9oyaLoLIfXcb/u4an9raOiYRhg4Ygy7A20LCRVGcHBkZeQQwzhVMVOr2iwDc96ntfoJG0wFe7/40tX1gOk0cAEpBN3tGplsbUfCqVeDN6OaWMEpgRxlmk9vF6Qd42TTQqI17pYtXq9vPU8Az31y9BaGiCLXaa4KuLoSHhw/tfqBXCktK8Zn75Ln1Eb3o7aeU9mrbXgrAsuQDAF5jsdzsiKLXjP96QRA+6HmwlwL4+PhcohjoBZyFaOFLqG7ks6ioqF7pak1mMWZA33K9PP0DL/hiGACAEGKyTU0qgJ+f32kK5LhWpP6B4B09QI5SqTxt6kSfecwHegHHoZR6RQ/Q19sPWPh0bONDc/5DG+tHuESqWxVKWZEiEYQQUeaj1bR1nPekOCRMWRL/+RcP9nXebJw6X1b4N0KZ47DiK+MD9EYQiS/liVVp+FyESK82LzJXwGzDRhyryKOgFpNIDmAaQfR4HMDHw/JK88wVsPhmEz1eJMBVS+UG6I3gWSfQVUqEFy0VsqgA4fnljSB4xSki9SNECo9GAhPglYT8cotp6awa25U+Q9YBKHFYqn6EZ7t/UnIxKGKdVSWtXamqHZ+cTUAPAPCab6F6M1qegPeMEgiMSKfFF5RZFehrtXUfkVt2CISutl+u/oWnegBC6GprGx+woQe4XjtTl5mwFyDT7RGuvyBSgg69BxSAYP+wk2U/scX7ZNv8nlKRpfwDABpsla0/4SHvbwPLcQ/Y6nq02cETdvxCHUMwF4DnfZxeiuB+619kwMyNO15k84cRbBsCulE3ccRqUPExuy6+BaEUQQBYEKBDR5rduh2QsH8fdqLoj3Zd6kC8GqNSd/wHoA/YW8GtCM/z0HS0Wy7oJAghG5VK5YOws0d2xMcvKvx8HgbFNgfquOVwZ/g3IWSbUql8GA4Mx44u8vAKhe99A7EDNxAEt22xzGlqaroPDu7pdGQI6I6PSq3eDZCpzqjsZkWv00Grdd1HSLrxPc/zd0RFRTn8pRdnLfN2dLT73UWAXCfVd1Oid0/3n8uy7F3OaHzAiev8YWFQ6fW6WQD65S5jSkWXRwATQk5pNJpZYWFhTtto4NRAj6CgoKtUFGYCZL8z670Z0Otc/vbv53l+ZlxcnFOX5p1lA/SEbVOr1xKQp11RubdBKUW7WuWyLWCEkL8qlcrn4YJUs65SAACAWq2eS0H+AcDHZTfxAnRaDXSu2f7dQSldGBERscEVlQMuVgAAaG9vTxM7fQUxLr2Rh3Dh23+RUjo7IiLCpRncXB7s6evrm09FIR0gB1x9L0+g02ld0fgHKKXprm58wA09QDe41rLSl/jtm6dSXn9TRxnTDk0Y5bhBJHgQp2lVl1KtxiljM+VYUXrXnO8j0tP/BDcl7XKnAgAA6m9LGEUZ5n0AN73TSMMzzlv6JdhPRfaJhFPFxU6q0brbeip5Qf34pPspxVoQRHpEAAfhBQKt4JRl34sUeC7h5JktzqjMVjzWFStzz/6XkzLDQfEWbrLt6CIl0Dna+BQaEPzJl1WkeKrxAQ/2AN2pzUpKISLeAzDD07JYg0ZPHA362C4K9OnEwjKPbhsDvEQBuqjPTJ4BIi6jID+Hl0Yf6wQCvX1vvwiKbxjC/CX+ZMleZ8tlL16lAF3UThoaS3jJYoAuABDqaXm6sKvxKepA6McsmH/GnSytdIlgDuCVCtBF5bR4uVwtuQ+ELAOoJzdZQisQ8NY3PgXIt5TQD1sZxVfpJ054rY3j1QrQnfoJiRMAzKeU+SlAY911XwpAZ/0mj0YA/wbD/WNYXtFN8Smem0YButOQkThM4JgZhGImQKfBRcOEIEKnFRhpnz8RRR0hOEQJOURF/nBCQUWBV2SEsIGbUgGMIIRczkgYy7BkBiiZAdAMOKgQIkWDjieDTVj65RQ4zFAcoix36GZ5y81x8yuACWozhvhS4h/NERpNGTGGUiYGoNGUIIZQRAMYgs6PZXIAmkBxXgStFSm5KlJGqufRCAZXCHBFpLhCKK6wUq7Unrh7b+f/AWs50ONfEUakAAAAAElFTkSuQmCC";
            case "PHP" -> "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAACXBIWXMAAAHjAAAB4wGoU74kAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAGlJJREFUeJztnXdgVMX2x79zN7ub3WTTGyU9SA/4QIEgXRDEwpMmKIINH9h++oTYHkZ9Iio2UJQHKgJSAhZQFFCkSa+hQ/qmh9TNluxu7szvj5vEAFuyLZuQ/fwDu3vnzLk5586dcuYMwU0HI7htXSy8RD3AEAsgBoREgdFQgASDkGAw5g1AAsCnvpAGgAGE1IKxcoCVA6QUQB6AHFCWBUYv4tj0bIAwN92YSyDuVsBhBqV2AmODAQwGo7eBkF4AFC6qrQbAWRAcA3AAHDuAA9MKXVRXi9D2HGD4N97QS4eDicYBbCyAW9yqD8NlAL+BI9sRoNiD3+7Wu1UfG2kbDtAzVQI/OgbAFDDcD8DP3SqZoRoMW8BYKnjVTpx4yuhuhazRuh1gwHddQLweB9gsAOHuVsdGSgCyETz/Pxybft7dypijdTrAgNQ7QGgygPForTraxgGAvIfDU35pbZ3IVvTHZQSDNkwAJQtA0Nfd2riIk2B4C0embm0tjtA6HGDghnEA3gbQz92qtAgExwHyOg5N3eF+VdzJ4NSuoOxDMDberXq4jz/A4QUcfPCcuxRwjwP0+1kOseZtAM8B8HKLDq0HI4CPQbgUHJqia+nKW94BBm68E4QtB0Nci9fduskApU/h6PQ/W7LSlnOA4d94o1aeArB5ALgWq7dtwUCwAgafF3DiXm1LVNgyDjBgXSI4bh0YerZIfW2fs+DptJaYP3D9kzho4zQQ7qDH+DbRGyLuGAZunOnqilzXAkxOFUFJF4Pg/1xWR/vgQxy+NB9Ioa4Q7hoH6JkqgYJfA5ApLpHf7iA/wls7HXserXW6ZGcLxPBUX+jp92AY43TZ7ZvdYHUTcORhlTOFOtcBBqwLB+F+BfAPp8r10MAJcIa7cfCRUmcJdJ4DDFwfA5DfASQ4TaYHU6QDbAwOT8txhjDnOMCQ1FAY6H4QdHWKPA/WyATP3YFjU4odFeT4MHDAWj8Y6XaP8VuUeIjoDgz/McBRQY45QM9UCYh4MzzvfHeQiFr9Dxj3q9QRIXY7QErKbq/pEzv/Tggb7YgCHhxiBCpUGzA5VWSvALsd4NaRXY5+9+agob99PAydw+T2ivHgKAQTkEcX2lvcLs9ZsSXzs2ljou8DgITOvnj83jhU1Bhw4lKlvXp4cIwkdJp0GgWbL9ta0OZRwOK1lybNmRi/Se594zL+tgOFeHLRcRSVtfiytgewcvC4Fcem5dlSyiYHSPnmVMC0kfFFt0QpvM1dc7VKjznvH8f3u/NtEe3BORyBsXqILeHoNvUB+nUJ22vJ+AAQGiDF5oWDkfpOEoL9JbaI9+A4AyAOeNuWAs12gKWbrswfn9QxsbnXTx4ZifPrxuG+IZ1s0ceDw7D5GJQ6rLlXN+sVkLL8csis+zsXRofLxfaotOnPPDy16Dgqawz2FPdgOxdgrO7bnFdBs1qAvt19t9trfEBoDU6tHoNR/dva5p42Sw9I/JsVh2G1Bfhg/ZXxz0+K/0Xs5fisMWPAii2Z+PeS01Dr6hyW58EiWnB8Dxx8KNfSRVatekevwFXOMD4AEALMnhCPM2vHYtitoU6R6cEscjCvxdYusmjZzzdd+c/AXiEhztNJILajD3Z/PhLLk/tD7m33LKYHazA2CYPWj7J0iVkHSElh3NB/hCU7XyuBhtbg2NdjcFv3IFdV44GRtyz9bNYBIhIzFvaK8/cx97uz6BHrh4Mr7sSiuYmQiD3bBVxAEm5fP8Tcj2Y7gRezVdpuMQqZa3QyzdnMasx86whOXfGsKTgVgp049OBdpn4y+cgt3XRlfksbHwB6x/vjyFdCa+CsjqcHAAxjkLThNlM/mfwr9+8WOM+1GplH7MUheUZ3/LV8FLpFt9ZMMG0QHq+a+voGB3hvzYXRA3o6v+dvK7f3CMKp1WOQPKM7RFzrSGPQpiG4H0kb4q//+gYHuCXK723igr83054A9Jk2lfGWiLBobiL2fTkSXSJdlfmt3UBAyWPXf3mNA6QsPy5P6h1s8l3hCEx7CjR7KvisBwBDjs3lk3qH4PTqu5A8ozs4T2vgAOwx9Ft+zZT+NQ4QFhLwSligt5N7XxRQ7wf4GsBYDKY9JXxnI3JvoTXY/vFQRIZ7QtDsJAISxTUTQ9cYu0uk70POrI1d/QJMcwwQ+f/9pSgArOonsMpNdskcfXsEzn03FrMn3PA689AcGDe16cdGB0j55lTAgJ7BMc6si5Z+DBhyAVGT8HWRP6DPBiv70m65fj5iLE/uj18/GopOoS0+Wm3bMExAz9TGSJ1GBwjx9XlGIfdy6guWyHqDGXKvaQGIKED4Ttbs2BKzjBvUAefWjcOMcTEOy2o3EARAwYY2fGx0gKgI+SSnVybrAxiU17UAAYAhF0TWxylVBPiKsXrBAKS+k4SQAIf2SLQj2NiG/zU6QK/4gB6WitCS90FznwQr/xYAA1X+C/TKMICaT2VDZImCsUVNJnRECjCDEpD1Nl+ZsRj8xVtBi/4rqFvyUX3dX5stIoSgjcU/h3W2dBseBMY1/IcDgHdXXewX28HHcsSP+iBY9VbQgmSwys3gOi0Co9pGI5mCyBIFYze0AJwvAArUXQXxNp8xhha8DCIKBBf+EljpEtCS98Cqt4JpT1tUMSzQGz8sEgJSg/w8AakW6IF+azoA9Q4QHCiZYbWIz+31/2Gg+S+C1V4AF7UcrGItmPqg6TLSWKGF4KQACCDyAwx5INIuAGc6uJhVbgDTHAIXswpM9RtoccOmFwLi07wpiskjI3Huu7G4Z3DHZl3fLvGSJAH1DhARJB1u7Xou4lVwHf8rFGEG0JyZACcD1/EN0PznAKo2UYqAyHoCxkKA86nvAOYAcjOpgI3FoIVvgIv8BMxYAJr3HAAGEDG4qM9Bgqz7aQMdQmTY+sEQLE/uD4W8veeiNAHBYKDeAaIjfKwmbaT5L4EEPQwueoXw9PIq0OzpIH7jQHwGmn8VyPr8PRJo7ACafv/T/BdBgmeCSBNAc2YBzACI/MHFpoIoxoAWv2vbPTYJQRvRL8ymsjc/7HYA4FJSdnvFd1L4Wr28Zido5j0gsl7gYjcIxjQWgubMBBfxHzDNATD1gRvKEVkioDsHcHKAiMBqL5scArKK9QCrBRc8C3z2gwBfBYg7gYv/GRDJwaePANR/2XWrMR18sGvpCCxP7g8fmac1qKcXwAgnS+g4zEcmsjr+F3X5AxBHgk8fDfBVECX8AkgiwXRnQPNfABf1JVjhqwDVAACY5jBowXzQoreFJ1efDqbeD1b+LWjes6BFKWC19XkQjYWgVz8H1/lD0JxHAEM+iKw3RAm/AprDoBn3gfgOExzPThpag7Q1d2FIX09AKgB/JK2LIsu+z3h7zgPxrze3FKv6AbTgVZCgaeBCngTNmQWmSwMJehjENwlMtQPg1WA1u5ohjQMJmADwlSChz4BdXQZWswtEMQJc5BLQojfB1IfAdf4IRDHc/lu9Dp4yLP7uEt5YcQ56o0vS77UV7uZ85SKL4//rIQEPQNR1H6DPAlXOARf1BYjiTmE0UL0drHpbM40PABSs6gcw7XGwsi8F4wdNB+mQApo1GSBSiG7Z61TjA4CII0ie0R0nVo1Bv26BTpXdxojh/Hwk0TYX8woDF/MtSNAM8DkzQUJng/gOAaveCjA7NnzwNWCq30ECHwTxHQ6qfBKkQwq4zh8BItfFAfSM88fhlaPbb0AqQyzZc6o0b1jfUPunz4wloHnPgmkOCb12R+BkIH53gev0gTBn4FIYmsbEnsmowiNvHUFaepWL621NsFROIfNy7BEThwPgHTc+AFCdMFpwtfH1WWCqndd8lZgQgMMr72xnIWgklFPIxRb3+1uD1ewBs3N4ZlJe5UZAn+E0eTdWoAdVPgnUVdzwU0MI2l/LR6FrVLsIQQvm/Hy8TK4BsKtLwV+6XViEuboUTL1PGJtff13V985VifGgVT/e+H1dKZjqd7CSD0FzZoK/1A+s6gfLoqq3CQ7VxNg0fz6Y7hzAzO+cHtgrGKfaRwhaMKlWG6ifj/jau2RGoSdvUAqzeIZc4V99LiAOB5H1ESZ4ZImgeXNMPk2OQLy7g0S8BujSwHRpYLqzAF8DIokCJNGAJApEGg1IYkEUI80L4lXCBJKxCETeH5DGg1WsAwBwnRaCBD9uVZe/0srw6H+PICPf1FR3m6eSaGvrmEz69wZNpjsNaM/UT90K07fEyx/g6j/XldU7hRJMdwqsbKVLNOPC5wHSBEASLRhe5Afw1QBfBcarhNao/jPxHQZITc9mM/VB0KyJuD4OkfjfC67Da4Ak1qouKo0R8z5Lw4otmWCt4rQ/p6EldTxlDZ0epj0Omv2gEMBpDpECEPmDcP7C1K7ujEs0I/L+ANWA8dWCoetnGE0ijoAo7kezTkCL3gS7usx0We9bQPzvAxc4FZBEWdRpx5FiPLHwGPJLW+Q4n5aA9+IYhZ3pAtsQznlsB2ky8FvmYhgLHc7R3CqghBN5MWMdiEhwACLvDy5us02vALiqBVCMAKQJIE54BbCry2+Ub8MrgK9Ro3DB+yhbnYqb6R1ggAhe1GAE5/13LB2R9QW8ewqdQP0VsPoOoKlOIFGMFnriLugEQtZH6ARWfQ9qqRMY8E/zgngVaP6zACEg8gHXdAKJ76BmGV99+ASUT78MfbbSWbfXajASjnnxWh28/K5bDSZiQJ8OWr5GMLbPAJCQ2ULPX3RthnKiuBOsMtW5mvmPB/EbDfiN/nuurq4UTJsG6M4IK5CqbeAiXrMohqn3gwufD6IYDXgJSSgoY2CV6wFYjoCjtbUofu8zlCz9CqA354JRLZFQUn0pk/p1jbN7sMvUe0GznHg2FBFBdMs+YQTgCpgeNONukOAnQIKmmbxEc+wUcue+DH1mjmt0aCXkSEJ0nL5KzTsihPgOA/E1m4DCdnmBU11nfAAgUnBRKxpbhKZQvR6Fby5G+t0P3fTGBwCtyFvnpVVpjHDkAGdjCQAOIBKnLAaB6oRhqAtXASGNA5Fe+/7Xnb+M3DnzoTt3yXX1tjJ08NJwNWVqu48hY1Xfg8+aCBL2NIjPAIcVIv73g/iNA595N1jNHoflWakNAMDqeJR8ugKXR01sV8YHgBqRrNCrulJTBMC2FJ51paD584TQsJhvQQsXgKn3g/jfB6b61faYAJECxGcgWOUGgHDgolaAKZ8Cq+4PrkOKy1qD2kvpyJ2TDG2ay4/obZWoRd7ZnKpaa5Pbs6ofwF8eCkjjwEV9AaqcA1bzB0jQQyD+Y0H8x4MoLKamawIHEjARRN4fJORfIIpRYBXrwIpSwMVtAlgt+CvDwNR77bg9C/dQ/9RfGvFAuzU+AGiI5LxXeaV+K4AHrV5dVyqspGkOgItcAiJNAJ95n7DRQzEKJORxMOVccAm/ApwPmOaIEO6l2insC2iKNA7EbyxI4GQQ7x6AsRB81hSIYteA5paC1ewGzZ4OLmYtmGoHaPYMkMAp4Dq84XBroM9WQvnMK1AfOu6QnJsBNfP6naROThU98NW4OpGVyHD+YiKIVyi46K/AjEVC3D5fBSJLBBezFnz2JHAdF4H4Dr6mHKv6ATAoQSs3g4gjBOMHThHm+pteV7EerGoTuMjPwGfeCxjyhbDw2PUA04HmPgHiFQ4uYZt9d8sYylanouC1d0G1nhNNNJyU7Sqf6MVN2TSFr7qitDoUJH73gIvfBqY9LYz7+SpA0hlczFrQogUgPnfcYHwAwmKRrJewRYzVgXh3A3RpN8oPmgYQGWj5NxDFrK/fd1AAmnkvwGuFsHQ7O5oGZQHSJ8xE3gsLPMavJ18cWJOCFMoBQFFGgdW5XK7TQrCKNaDKOfU7dhRCE139M5j2pDCvbgptGogkun7uvgqQRIFpz5quo/OHYOWrwfQZ4GJWCUNLvho0ewqY6g9wHRbYdpeMoezbjbg4+B6o9x+xrexNTpE44ApQvzWsKK/8qLUCtHghaOHrAChAJOCiVwNUB1r0FrjOn9bv/L0eJmz+EHcEqBaMrwaRxJhsAQAA4ghwHd8EzX8BRNwJXOQSAARgRtC8Z8Aq1jT7Bo0lV5E1fY7w1GtumuVbp1Em8t0F1DtAaal2idUSmgYfIcJGDVkPUOVskOAZIL5Jpsvos4QgT1oLgAmrd5JIMH26MOFjAhL4IIjPINCcWSB+48BFNOQ3ZEK+oWZQtWU7LiaNR/WO3c26vj1SKZWvA+od4OEPHt+pycqzvM7pmyQETnRaBBI4SdjDz/lYXJBhurPCCh5fLXxBNUKVXqFgtRfMluM6LQLjK0FLFoOEPQcuPBnE/z4Qc7uK66m7WoasGU8j+9HnwVdWW7y2PaOUhOi/uLzyDNBkCjj72KWyXnGRZjfNceHzr/0cZT3JE9OlAZJoIaqnAV4lOIXuDCDvZ7qgOAKi7qcaP5LwF60ebVK1ZTvyXkpBXbkn0bQ1MiVhjUEcjdthcjOv2jm+soAuTQizahpNzFcJTuGkQBK+WoXcOfOR/ejzHuM3k1Iv38b1+0YHKC3Wz69TOTfylenO1Y8A/l5uEDqC0U6JJFL9sQ8XB41HxcYtDstqL6g5b1aj9mtsvhsd4LGlj13N3XfKQjSo7XBhLwgRPHyTJ5OvAqSxICFz7JbLq2qQ98ICZE55EsbiUido2n446905fdnVZY1P+jU7Iq+cyfvZmZWR0DlCXp8mLQD4apCACSCB9mWlq9lzAJcG34uybzc6Scv2RZ40cFXTz9c4QHaB+Cl9SZmTq+QA3yHCHL44AkR+6/XVNguq0yH/1XeQMfFxGAqKnKxj+6BCJKcVtT6fNv3uhs716bXbC/o8dJfT02sx7QkQUQAgtT3Hr+boKeQ+nQx9psUj8DxYYZei54F5yh/vaPrdDY/i+bMFr7gi9JnI+9ls/IYQrSt3T/cY30EYCJRcwA2TNiaH1/l7j+s7De3n1kyLmhNpUM59GbXpWe5U46bhtCyq5LHCPyKu/97ky/jE7kvmc7K6GGasQ/F7nyF97DSP8Z3IRWkHk8fKm51gqzp3hffv2aVF86boLlxB7txk6M6Ynyb2YDsZknD1lJL9JiNpzBr40C8nLG++dyKNgZkjH/AY3wWclEWbPUPY4hR7+emLfFCfbi5tBWovZyB3bjK0p865spp2S4YkXHOhZJRfClJMbm+yaNxD206udo1aAChF2bcbcXnkJI/xXchx75g3zBkfsNICAK4ZERhy85H7zCtQH7Aah+LBAY7LY5WzC3ZYTANotXnf+2vak8xgPp+OTTSEaN1xr8f4LkbLSdg578gHrF3XrE2hp9duL+zz0F0dHFHIkFcI5bOvoGbfYUfEeGgmv/knbn0tZ/P91q5rVgfvxJmC/tqcfLunB6u2bMelYfd7jN9CKCUh+lyDma3P19Gs3DBbD2ytGRDYL6LrsN79YcO5ssbSMuTO/jeKF38BVqtvdjkP9kNB8Jui9/8tzfvGzDEu12JTXoCzG3aW9po6ulm51qu2bEfev99AXUV7Sr3qfnYpehycp/zpxg0aZrBpjH/sQH5P1cVMi+ky6q6WI3vms0KIlsf4LUq6NEKdXhc22pYyNqUH23J0i7avb5/K7kndxnHiG1OsqHbuQebU2dCeNL3xw4PrUIlk9HffHkOWKb+yadnU5vxwmw9sPTrIr1efhME9uzf0B/hqFQpeeQcF/1nk2YThBhgIfvHvu3Bx9pq1tpa1OzfQme92FPeePiZc9edfUD732k2TO68tskvR49A85U9mdudYxu7UMJd+OhRVsH1/Xvi2TWE3U+68tsYxeaxSo6waav1K0ziUCnt2x9nyO1hGbq/a/BBH5HiwjwuyjpVH5L1jlmYstTvNj0Mrff8r/J/2ZEBiYpY01PPib2EypWGaY0F9ujtifMBBBwCATy5+UrRb3uPWfHGgE44M8dAcisUBxv2BXQd+eu7TEkdlOSVL9LHKk+Vh4XftCeNVMxS09mbPPO1WCsUBxh2KPsOXXFl5whnynHocxlNxT3UZqb14sou+2OpJpB5sJ1Maptnn2+X2pRnfOC1syqlP64nKExWdYiZ87WWsnRVWp5I7U3Z754KsY+VeRY8ey9JXOjVS1iUH4swNnevbR5J5/jZttuUTGDw0i5PymMK/Qjv2XHV6ldPn1l12IlIKhnv5Rfr/NUx9aQBx0oEN7Q0Ggj8V3Q9plFVDU7DHjhM5rePyI7GS42fMH1197t0gXtMOj+a0H5VIRrf79n5rUc7aN11ZT4ucifZ83KzbBmqz/uiqL3b1caA3BemScM1Bny5jP8362nkHMpqhRYZsRypPFypCHv1EJaVDYvVlMTf1SXwOwECwz7dr2lHEdlumXJnZEnW2uC3mx898Lkmd/n6UoUxq/er2g1ISXHtAnvDSB9lrPm/Jet3yMM6KSfFOYOfXDVdfnCCnhnbdINQSMfYrbtmbq+1wT9PMHS2FW//4zyY8fmuP2sLNAzSZpo/8uslJk0WVnJTHTFiasdJt0bKt4ulLjn34pb66/Ne76gv93a1LS5AujVCfkEanvJ+75kN369IqHKCBefGPPNtbm7/gZl1eTpeEa07Joj9w9dDOFlqVAzTwYuysyV2MJYtu02bHCSebtl0YCM57d6q46N1h2bvZ3/3H3fpcT6t0gAaejnl0QGdW88Y/tDmjQ+tU9h9s5QbKRT70jCzqaK4k+NUlmV+32qTFrdoBGpgVk+IdTjJfijJWzOyty4v3pbWtUu8aTsrOy6Iycr2DV5XUxX20Kiel1t06WaNV/iEtMTd0rq+fQvV0uLFmcpyhtFekodyt8wlKSYg+SxJytkTiv6lGpVjmjqGcI7Q5B7ieOV2fSAwyaGeE1KlHRhirEjobKxU+VO+S+9JwUpYvDqwpFgdkXOUUu9Ry79VLLn7VppMbtHkHMMXzcY/dIWOGET68saec6WMVVBchZ3qFnDfIfHm9WMoMnAgMDZNQWk7CeBDoiYSqRVKjViTRaYi0Rs3JijUiaZaGSM7rOO8/l2SuaNZ+u7bE/wMSBP78T2w/aAAAAABJRU5ErkJggg==";
            case "EUR" -> "data:image/png;base64,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";
            case "GBP" -> "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAACXBIWXMAAAHjAAAB4wGoU74kAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAGhVJREFUeJztnXdAU1cbxp97E0ICCTJEUURkiwtXlVoXSrF1ouJWrAuVWmu/OqvY66zVamutA7S2rlZxa+tGcO9J1TJFBMTBhrCS3O8PDAWSkNxwIQHy+wty7jnnJfflPeeec+77EKhjUBRFNst3HNDwXcJYgSS/pUm+uIlxXo4ZV5xrROaJOWRODkEXFQLFEsjE+QAA0kQAGHFB8IwhE4lomamJVGIiLC40FWWLBSav8o0ET99ZtdifJIj/m6IomY7/RFYhdG1AVflu+5VOliL++CbWIi/7puaOjs0thNnrNhFvfvmN1X4azZoEs3lf0HEv0nNfpGTFpb7NCU/PK96zaGq3B6x2VMNwdW0AY1pQfHAkvedO6z4rcFzXjx2amfMqXpJdTV0LTXiEh7uNyMPdpj2A9gC+GumTXrRl3+1z63+9/guQEYHYTYXV1H21UDscoDXFQ5HEBzRGAtIhAGHWzMYMDs3MdW0ZHO0seHZNzAYC9EDAPAvOQcdBEKEwSz2HeyHFurZPHfrtAI5LXECSU1Ao/QwgGuvaHA1oAMAfNO2PrMav4RR0AARCELviia4NU4V+OoDj4u4gyQUABgB0bZ2nNAaB2QBmwznoGkB8j9jlfwGgdW1YWfTJAQg4LfUFIVsKEO11bQzLfATQJ+C85D5ALkfs8hPQE0fQDwdwWvopCHoFQHeqAw8mlUB0BOhjcF56F8ASxC4/q2uLdOsAbkvcICHWg6AH6NSOGofuDOAMnIIuAJyvEEf9oytLdOMATSkTmEhXQIrZIPQkCukCAt6A9D6cgn5EYS6FpB/za9oEsqY7hOsSb5hIIwH8D7qOQPqBEQjMB1/4GE5L+tR05zV3A1pQfHBlFGT0POjC8fQfZxDEBbgEbUce5yukUOKa6LRmboQD1Q5c6V2AXlBjfdZOCNAIgIn0JpyDWtdEh9V/M1yWjgFHeh1AjfxBdYS2AO7AaenE6u6oGh1gBAfOQT+Cpv8AYFp9/dRZBCDo3+G89AeAqrb7VD0Nt6Z4cG75B4A51dJ+vYL+Gi7SQ2hB8aujddYdYNX2m41/Wz44qmkj0Ui2266vNLYSDt25blDUyq3Xbdlum1UHWLU1vI2vl2P8Z8M6tHh4MhCf9HRhs/l6iXc3Jzw8EYhJfh2b+3o7Ra/edrkVm+2z5gBrgq92Hzuw/f1WTtYmAGBtaYpTO8ZjY1B/GHE5bHVTb+BySFCzvXD2N3/YWAsBAK2dG5mM7t/24Zrgq93Z6ocVB1gdHN5yxKetwlo0Mzcq+zlBEJjt74kr+6egha3u9+5rC3ZNGiBi32R8+4UXSLL83oi9tcBo6Ms7ETunrmNl0ajKDrBy63XbIX1b33O0s1A4mSOnq0cz3D02AwN6u1a1uzrP4L4t8fBkID7q1FyhrCA6HtE+oyHetoPTKfLShd8CNrStan9VcgBqc7jw427NH8nDfmVYmZvgZMg4bAzqD56RYUioCJdDYs28j3Fs6xhYNhAolKcfOIFo75EoeBYDAJDFPSfaxd6+v/OL1dZV6VdrB6CocG6vTvbPurSztdK0jnxIuHZgKhztLLTtus5hb2uOK/unYEFADxBE+ZAvy83DixkLkDjrG8jyC8qVkY8juW1joxM3jNig6DEaorUDdOhuc7t3V4dm2tTt3NYWD04EYtSANtp2X2cY6uOOB8dnwrO9nUJZfuQzRPUdiYzDf6usz71zh9+Jkxinbf9aOcD2A3d+Gdy3ZQdtOwUAM6Ex9v80ErvXDYeAb6S+Qh2Db8zFxqD+OLJ5DCxUhPyY/uNRGP9CbVtmYReaHJ9MHdTGDsYOsGvujumjvd0+16YzZUzw9cDV/VPh0kLjkaTW4+pghRsHAzDb31OhTJqTi4Rpc0tCfoGGJ8xpGi1uhPttXrrLi6ktjBxg+5c/Nfa4fHRrst9kFMYlMO1LJR1bN8G9YzMwdlA71trUV/w+aY3bh6ejvbuNQpn4fiSieg9H5rEzjNo0drSHy4Gt6DG23wkq+KTaCXlZGDmAe3LsEzo+gSgdmw7+xcjQyhCZGmPfBj/sXjccJoK6NyTIQ/7BTaPQQFRhWZ+m8TZkL2IG+aMoMZlRu+aDfOB6bj8Ebd3R1q2xsJ2DUxiT+ho7wOEp1HbTSxGlcVqWJ8aLwIVI/HwRZHnsnV2Y4OuBu0dnoI1rI9ba1DUtHRvi1qHpSkO+JD0T8eNnIXnxGtBFmr9HQvKNYbtqIVrs3ABOA1Hp577eLT1/3n1jtsbtaHLRzsD1di73rk0BrXiSOT30JKK8RyL/SZSmfarF3ckatw9Px5cTP2StTV3hP7Q97h6bgXYtFd9rEd+PRPTHo5B97hKjNvmujnA9tx/WAeMVygiCwKc9XdZqOhRo5AAu7xLvSJNSVJ7XLoxNQEy/sXgbsleT5jRCwDfCT0s+xe51wyE0UbnIqLcITXjY88Nw7Fo7DKaCCvbLQ/7ACYxDvuXIQXC9EAq+u+qNNmd7K2PXpvahmrSn1gH2Tv9+huhKhNrXsmSFhUhevAbPP/sS0kz2Xs+c4OuBu8dmwKOl4qRJX2nlbI2bhwIwfoiHQpkkLQPxYwJLQn6xROM2OSIh7IPXovnm70AK1B8NGNK35YA1IZe6qbtOrQO4Jz3dyMTQrL/DEOU1HHl3HmpcRx1uDg1x4+A0BIzuzFqb1cXMsV1w79hMtHZRnMPkXruDqN7DkR12hVGbJh6t4BoWCoth/TWvIzCCp4f9AXXXVeoAm/fcDmr1wxIev6Wzxh0DQFHSK8QOnojUtVsAGTv5FAR8IwSvGIzDm0fD3KxaDsdUCTOhMf74cQS2LBsIvnGFw9Y0jTc//4q44VNQnPpG80YJAtbTxsHl1D4YOyhuDqmjVxf7Zpv23Jxb2TUqHYCiKLJnF/sF/JbOcD1/AFYT/Bh1TkukSF23BXEjp6P4zTtGdStjmE8rPDgRCBtrkfqLa4hGlqa4d2wmxgxU3JyTvEtH3KjpSFnxI2ip5v8MXEsLOO79BbarF4Hgaf9Y3KOT/dLKylU6gI3bgNVtXBuZAiWPHHYbKDT/ZTVIU0brDMi5dANRvYcjJ+IGo3qV0cLWHH79WD0YUyVG9m8DZ3tLhc9zr9xCVK9hyAm/zqg94Yed4RZxGGY+vapsm4e7jWjj7psqV25VOkCPzs0VniUtRw2G24VQCNq0ZGSE5G0a4kdPR+raLYz+CyqDw9Gf1wsq2kJLpEhduwWxftOYRb/3Id/p8A4YNWFvHaRLG1tKVZnyb9F1iXfgt38JUt7kKBQZO7eAy5l9sJ42jpERtFRWMiQMn4LiVwzGwVpGccprxPpOQuo6ZvMfbkNLOB3YVhLyjdh7YetNWh6WbgxrCMegD5SVK3cAGfH1pdsJ8Bi4GacvxShWMjaG7epFcNi1ERyLBowMKp0JX7jMqF5tIPtsRMkT0K37jOqJenrC7dIRiLw+YtWesOvx8Bi0GeevxQEkvlF2jaIDOC5xAdAPAN5liDFg2l7MWXkaxRKpwqUN+veF28VDMO3CbGdYkp6B+LGfI/mb7xgtf+otMhqpa7cg3n82JOmZGlcjuBzYzAuE08EQGDVqyJo5EqkMyzaFw2fSLqS+zZV/PATOS5wqXqvoAAQxFWWyNNA0jY27bqD7qF/xPClD4XJesyZwPv47bOYFAiSDcZmm8Xb7PsQMGI/ChJea19NDMk+eYxzyS7+3+Qy/NzUkpWbDa/xvoH4Oh0xWbumeAIjJFa8v33OnACMQ8FfW8O3HSegweCsOnVHMd0RwObCZr50nix8+QXSfEcg4eppRPX2i+PVbRtdrGznVcfJiFNoP2oKrd1UeIpmMTgHlninLO0CWzScAVK65ZuUUYOTsUMxZeRpFxYpDgrZjmTQnFy8C5pXsLFY491aXIHg82K5aCIfff2I8d6oMecj3nfkH0jIr3Zm1QWajvmU/qBB7aLWvc8mHhI9G7UBcYrpCeelsdtVCxrPZ9NCTiP54VOnJ17qEsVMLuJ79o2QHj2AvD1JiShZ6jd2pLOSrgBhV9rf/HKDk5cPBmnZ8NzIZHYdsxYG/laS3IQhYB4yHy8nd4Nkxe52tICoO0T6jWd1Z1DWWIwfBLYz5+ok6jp1/hvaDt+D6/UTNKxHwRWuqdHvyPwfgSHoDMGNiQHZuIUbPCcXE+UeQX6A4mzfp1A6uFw4wXtGSFZTsLLJ92KSmkR/aaL75O8YrqJVRWCTBnJWnMezz/cjIYpxWyBzFkp7yX0pjEfXzxbPO9lY+2hpl0UCAHp2bQ2RqrLS8MPY5xI+fMd4cIkVCCLt2BMdcc99MWbYe1ZEsuum3XystK4h5jvxH5SfHHDMRTLt2LHdahw3yxEW4ei8R7zK0/8eISUg7tezLPgOAMjmC/H07eDnYVd/7e8bODjB2dqi29nUJ38UBfJea+dtMTXjo14PZ7mxFYl+k9V32ZcnPJFCSct3BzrzuncQ0oBRneyvjVSHh7YH3DmAlEkzQrUkGahorkWgc8N4BbKyFvXVqjYEap4m1aR/gvQPYNzV31K05Bmqa5k3NnQGApKhwrpO9pVDXBhmoWRztLEQURZFcu4L4IWkLwog0XVvEIuKH7Odezg67Ckma5jt9tQDCjte8H9f6bcqI9ONHdW2M3lPwLKbOLVFbDhk9huQXi9ldnzRQazApzncnBQXi2vPGhQFWERSIbUnjvBxG6/8G6g58cXYD0kicZ1gBrKdwxGIeSeblGlJ21VO4ebkcksjJrcsqTQYqIyeHIOmCunsEy4AaCgtBorgOHMs2oB1FxSBpgwPUW+iiIoN+T32HJIwMT4H1FYLHAwmDA9RfeEYgoUG+GQN1FD4fJC0U6YWKtQEdIDSTkTKhqeI7XgbqBVKhUEpKBKaG58B6ikTALyYLTUXsJfUzUKvIF5plkWKByStdG2JANxTwTJLJfK7gX10bYkA35PH4T7mvbV32fvjt16N1bQybZF+4jNxrd1htU/jRBzDz7qn+wlrE7bec37mvyKjTgkkLaZGpcZ3ZFpakpbPuACYd2qDRrEmstqlLsnML6Vfr11wiKYqSxSdm5KqvYqAuEfcyI4eiKBkJAC9SsrRWnTJQO0lMzowG3r8alvo2J1y35hioaVLf5YYB7x0gPa94j27NMVDTpGXk/wG8d4BFU7s9eJ6UWaRbkwzUFDEJaYWLA3s+BspkCPk+5Orf1haCodo22sq5EYb6uCvmyq8CxalvkHnsDCQZWQplZt49YfqBoiKHPiHNzkXm8TMoSmK21sa1tIC5bz8YNVaUBS4skuDkxSg8epaqtV1vMvJOlPYl/yH4zzvBAM3YAfjGXHw/3wdjBlVZyPo/3mcRTVm2XmUqWa6Vhd47AMdMCMsxQ/F6QzBS129jlB8pZfkGNP32fwrCUMY8Lvw+aY38AglmLj2JvHwtAreMCJb/WOZIWEYEAMV/tUpwc1Ath6Yt0uxcJEydy1hGTV+RZ1F13LMJXEvNczDRRUUlGkwTlWswTfD1wJ2j09HWTa2cU0UyISBLNWv+c4DYTYUAjmvaSmVyaNoifvgEUX38kHniLGtt6gtmPr20Sg+bdUquwfRIoczdyRq3DgUwldc7gidUadgofyiUINRKjQn4RghZOQS71g5jT85NLqM2YDyKXiSx06YeYmRro1Vi7RINJn+lGkzM5fXockJS5a2wJc+ChsoZSyvnEkHHaaM6aWy8OqTZuUiY8nWdCfnqKE2sHRoMrrXmgtlyDSZVKenl8nrKNInLkIoGb8qt+ZR3gAhKAoLYraym/9D2uHOEXUlX8YN/EOU1HJknzzGuq42Klj4h6vUh3C6EwrRrR0b1ss9GIKqPn1JRihJ5vUqGBIL4FfdCyv2XKcYhmtwBoPScoMi0RA5t19ph7Ik6V0E5kzQuSb9q9nHt35kzatoYzsd+YzwkFCenlsjSKBkS+MZc/LTkUxzZPAYWDQRli2gQ5M6KbSn2GkfFgsBpoETW/f5x5XJo2iJJz0T8uM+1Cvl8Fwe4nPtTqWZubUVbrYVSWb4RAUqFqYb6uOPB8ZnwbG8n/+gvRFPxFa9ToRlErw8Y3RnXQ6cplUPTllKx5PPM9YLkmrmCVq6s2aNPiHp6wvVCKEw9mc2vci7fVClNZ29rjiv7p2BBQA+AxDpl9ZU7QNzKi19+9qHYmMfSql5VQj7fGHY/LivJuG0iUF+hFmPUpJFWQ4LkXTriRs9QKk7J5ZAYN7hdLqJXKNWrVdnLpdsJGzW2oDLj5AJRDMWSAYDv5gTXc/thNX64Qpmy9PS6QpzPni0Eh4TN/EA4H9rOTH5HJlMpT3vlzosfVFVT6QCBY7t8Exn1Ok9zCxQR349EtPdorSTiLEcOguv5A0pl0iNuPce+E4+rYhqr7DvxqESajUWEPbrCLeIwRL0ZLfIoCFT/E/0m723c6RWqrq80zly+82I1o97llA35LxmGfKGpSpl0mYzG9yFX4D1xl3Zr4NWEuKAYn07Zg2WbwiFlSRkVALjWVnDcH8x8SJBL1H/zHSJuxFEURak0qtJWZ03ouvr6g5eMJLEk6e871yLkC9q1gtvFg0pl0lPe5KCv/+9YuO48q18yW0ilMlA/h8Nn0m68equouKotpUPC4R1KdwdVQtNIjowTf/FZN5XhH1DjAABw9XbCGGUKYcrIvXG3XPhhgpX/CLie3qt0gSfsejw6+W5FxK3njNutaS7eiIfHwC04c5ndrKLC7l0YDQmEqSn+tXNXK/mu1gEWzOgZdioi+lalF70P+XHDpzLWBeaYCdHi1/WwW/8tCF75tWwVCph6z9v0PPSfqlpxVVu4DS1LhwRCjXj2u55eTyZunatWjFGjgeXRP4n9E5IylU51JWkZiB89U6uQb9KhDdwuHoL54H4KZS9fZalSwKwVyOX1eoz+FQnJ7CWZlg8JTodUDwmkYws6TmChkXijRg5A/e+T9JPhUYtouvyNyL3+PuRfvKpJM//xXibd5a894Nk3Uyg+eTEKHQZvrUwBs9Zw61ESOvtuw98R0ay2WzokeHUrX0CSiGnf7afpIQs1Otuh8dRytr/n+pMXox8CAGQypK7fhrhhkxWeOdXBtbKA459bSmTSeeX3FhgoYNYq0jLFGBSwT6XiqrZwG1rC8c9t5YaE7D59k/xCFv9P4zaYdPjgaXwv14a810ar1vBzwq8xNBcQftgZ9sFrYdREcUcxMSULo+eE4saD2i0krQr5kHDtfiIObBwJRzsLVtqVDwnCjz5A3Jptkn/NbNowqc8oSxg1u3/27ZAjXyvbiqzcypKQ73R4h9KbL1fArKs3vyx3I5PRYfAWhJ5iV9SiqE0b2flPRnXXNPTLYZwmbuKWuVsSvAcd1VT/lmtlUaIlvHqRgpawXAFzaOCf2ihg1lqycwsx6stQTA86gcIiZhNnZdA0jdBTkd/Nm9Wv8qc1JWiVJ3DIzm+H5Xr1fa3uOmH3LirVxKOfp8HTLwQbd93QxoQ6Qcj+u+g2cjtiXyiKcDPh6LlnN2aO7bpEm7paJ4p8RVs0L/L0VDpTIzgkbOapflQ5fPYpugwPxsMqnG2vK9x/8godh2zFn39FalX/4o34xMibb7Q+HaO1A4w8SBU9bOrSVNahfblFeaOmjeF0dCds5isuVhQUloR8v1n7kZVjSFItJyevEGO/OoiJ848w2lm8G5mcceVuVFuK8tJ6HKlSqtjpIQuzHjt6tCFdXWTAf+fchB92Vrj23/h36OoXXK9Dvjp2H32ID4Ztwz/R6h+tn8S8yTt3K8mdmt2/SjmeqpwreFLw/Jh7HT7qJpg3R6rqpOueY4/Q2XcbHv+rdtpQ73ka+xZdhgdj+4F7Kq95kZxVfDzsqefiaZ5V/kJZSRY9efO8W8fM3Xs/T84uF79yxUWYMPcw/Ocd1qvtW30nv6AYAUuOY+L8Iwrf2/OXmcX7Tz3qvXimFyvPkaxlC184vfvVfafutH787+tcoMSTPf1CsPe44hstBjRj99GH5SLnk5g3eftPRbZfGNBL8QCglrD3Ki+AoBk+MeT2m85nrsRcXrYp3JXNo1L1FfncaX5A92geT9BnycyezE7YqIFVBwCAxdM8X6M11RZF0l0A6lT2MV1RUCA5svzHK+OQQLH+6FQ9ghFPqCLEcMaBxlqUecnEAGNogPgOcZwR1XHzgWqIAP9ByRCHBXBdch4yYi8A9l4jrh+8g4yeiPgVp6qzk+qXjIleeQHgdAZBMDw0UJ8hboPL+QDxK6v15gM14QAAEEslwZb0AohlAPTvRKf+QIPGz2iQ2h3/Ugk10WE1DgEViKAkACg4B4UD2AygdY31XTuIBInPVb3BU13UvGpY7IpLaMZpD4KeA4C989O1FzFALIMxp3NN33ygJiNAWUqiwUa4UQchlawBiAk6sUP3/AUZ53PEU4m6MkC3uoFRVApiV/pDRniDRo17v+4gboKm+yJ2xSBd3nxAVxGgIvHLwwCEwWVxT9DkNwAUz4nXCYjbkMmWVfejHRP0wwHkxKy6DOAyXIM8IKO/BoixAGq7vD0NGmEgiJ8Ru/ykro2piH45gJzoFY8A+MNp8WIQxFiACARQu5IC0XhVkm9Jth1xK/U2G7t+OoCcuFUvAXyP3tR6vJR4A8QoEPAFoHnGxZolA6CPAeQB2JFh7ye7eo1+O4Ccki/yDIAzaE1NR5Gs18tXWbNiX6T1c7a3MtalaTEJaYWJr7LPgKR/gRH3cmkSxlhdWqU5tcMBylLyBZ9fvwjn1y8CVm253M7KwmRCE2vTPs2bmjs72lmIAFSL/E12biEd9zIj52VKZuyrN9lhaVni3WwdzNAVdUYnqCwh3+z61Pp14hiTYnErQUF+U2NxTgOuOI/HzcvlICeHQGEhaIkMdF5JAhTC1BQElwSMjQGRiJaYCqVSE9OiAhNRVj7fJDmPx3/6rlGzPwJWT65zOWz/D44Duwr8zHntAAAAAElFTkSuQmCC";
            case "JPY" -> "data:image/png;base64,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";
            case "CAD" -> "data:image/png;base64,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";
            case "AUD" -> "data:image/png;base64,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";
            case "CHF" -> "data:image/png;base64,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";
            case "CNY" -> "data:image/png;base64,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";
            case "SEK" -> "data:image/png;base64,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";
            case "NZD" -> "data:image/png;base64,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";
            case "MXN" -> "data:image/png;base64,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";
            case "SGD" -> "data:image/png;base64,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";
            case "HKD" -> "data:image/png;base64,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";
            case "NOK" -> "data:image/png;base64,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";
            case "KRW" -> "data:image/png;base64,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";
            case "TRY" -> "data:image/png;base64,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";
            default -> null;
        };
    }

private ComboBox<String> comboWithFlags() {
        // Creates a standard JavaFX ComboBox for selecting a currency code (String).
        ComboBox<String> cb = new ComboBox<>();

        // === Customizing the appearance of items in the dropdown list (CellFactory) ===
        cb.setCellFactory(listView -> new ListCell<String>() {
            // HBox to layout the flag and the currency code side-by-side.
            private final HBox box = new HBox(8); 
            // Placeholder for the flag graphic, initially empty.
            private final StackPane flagPane = new StackPane();
            // Label for the currency code (e.g., "USD").
            private final Label label = new Label();

            {
                // Initialization block runs when a new ListCell is created.
                box.setAlignment(Pos.CENTER_LEFT);
                // Set font and color for the currency code label.
                label.setFont(Font.font("Segoe UI", 14));
                label.setTextFill(Color.BLACK);
                // Add the flag placeholder and the label to the HBox.
                box.getChildren().addAll(flagPane, label);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                // Method called when the cell's content needs to be updated.
                super.updateItem(item, empty);

                if (empty || item == null) {
                    // If the cell is empty or the item is null, clear the graphic.
                    setGraphic(null);
                } else {
                    // Get the circular flag StackPane for the current currency code.
                    StackPane circularFlagPane = flagFor(item);
                    // Replace the flagPane placeholder at index 0 with the new flag.
                    box.getChildren().set(0, circularFlagPane);
                    
                    // Set the currency code text.
                    label.setText(item);
                    label.setTextFill(Color.BLACK);
                    // Set the entire HBox as the cell's graphic content.
                    setGraphic(box);
                }
            }
        });

        // === Customizing the appearance of the selected item in the button area (ButtonCell) ===
        // The structure is identical to the CellFactory, ensuring the selected item (the button) 
        // also displays the flag and code correctly.
        cb.setButtonCell(new ListCell<String>() {
            private final HBox box = new HBox(8);
            private final StackPane flagPane = new StackPane();
            private final Label label = new Label();

            {
                box.setAlignment(Pos.CENTER_LEFT);
                label.setFont(Font.font("Segoe UI", 14));
                label.setTextFill(Color.BLACK);
                box.getChildren().addAll(flagPane, label);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                // Get circular flag
                    StackPane circularFlagPane = flagFor(item);
                    box.getChildren().set(0, circularFlagPane);
                
                    label.setText(item);
                    label.setTextFill(Color.BLACK);
                    setGraphic(box);
                }
            }
        }); 

        return cb;
    }
    
    // Main method is typically the entry point for a JavaFX application.
    public static void main(String[] args) {
        launch(args);
    }
}