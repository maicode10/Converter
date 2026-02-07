# Currency Converter
A modern JavaFX desktop application for converting between 17 different world currencies with a beautiful gradient purple UI.

# Features

17 Supported Currencies: USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, SEK, NZD, MXN, SGD, HKD, NOK, KRW, TRY, PHP
Real-time Conversion: Instant currency conversion as you type
Conversion History: Automatically tracks your recent conversions
Modern UI: Clean gradient purple interface with flag icons
Currency Information: View current exchange rates for all supported currencies
Swap Function: Quickly swap between source and target currencies

# System Requirements

Java 8 or higher
JavaFX runtime (included in Java 8, separate download for Java 11+)

Installation
Option 1: Run from Source
bash# Navigate to the project directory
cd path/to/currency-converter

# Compile
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml converter/Converter.java

# Run
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml converter.Converter
```

### Option 2: Using NetBeans IDE
1. Open NetBeans IDE
2. File → Open Project
3. Navigate to the project folder
4. Right-click the project → Run

## Usage

### Converting Currency

1. **Enter Amount**: Type the amount you want to convert in the "Amount" field
2. **Select Source Currency**: Choose the currency you're converting from using the "From" dropdown
3. **Select Target Currency**: Choose the currency you're converting to using the "To" dropdown
4. **Click Convert**: Press the "Convert" button to see the result
5. **View Result**: The converted amount will appear in the "Result" section

### Additional Features

- **Swap Currencies**: Click the ⇄ button to quickly swap the "From" and "To" currencies
- **Clear Fields**: Click the "Clear" button to reset the amount and result
- **View Exchange Rates**: See current exchange rates (per 1 USD) in the left panel
- **Check History**: View your recent conversions in the right panel
- **Clear History**: Click "Clear History" to remove all conversion records

### Keyboard Shortcuts

- Press **Enter** in the Amount field to perform conversion

## Important Notes

⚠️ **Exchange Rates**: The exchange rates used in this application are **sample rates** and may not reflect real-time market values. For actual currency conversion, please consult official financial sources.

⚠️ **Base Currency**: All exchange rates are relative to 1 USD (United States Dollar).

⚠️ **Input Validation**: Only numeric values are accepted in the Amount field.

## Interface Overview

### Header Section
- Application title and description
- Help and About buttons for additional information

### Main Sections

1. **Currency Rates Panel** (Left)
   - Displays current exchange rates for all 17 currencies
   - Shows currency flag, code, full name, and rate per 1 USD
   - Scrollable list view

2. **Converter Panel** (Center)
   - Amount input field
   - From/To currency selectors with flag icons
   - Swap button for quick currency exchange
   - Convert and Clear buttons
   - Result display area

3. **Conversion History Panel** (Right)
   - Shows recent conversion history
   - Most recent conversion appears at the top
   - Clear History button to remove all records
   - Automatically saves conversions

## Data Storage

The application stores data in the following text files:
- `history.txt` - Conversion history
- `error_log.txt` - Error logging
- `user_behavior.txt` - Usage statistics
- `precision_log.txt` - High-value conversion logs (amounts > 10,000)

## Technical Details

### Built With
- **Java** - Programming language
- **JavaFX** - GUI framework
- **NetBeans IDE** - Development environment

### Architecture
- Event-driven architecture
- File-based data persistence
- Base64-encoded flag images (embedded in code)
- Custom ComboBox rendering for flag display

### Currency Conversion Logic
```
Converted Amount = (Input Amount / From Rate) × To Rate
All rates are relative to USD, so conversion goes through USD as an intermediate step.

Developer : Maira Lorraine Domaog

Version
1.0 (2025)
License
Educational Project - Created for learning purposes to demonstrate:

JavaFX GUI programming
Event handling in Java
File I/O operations
Basic financial calculations
Custom UI component rendering
