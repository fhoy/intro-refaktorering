package no.kantega;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static no.kantega.Pub.Ingredient.GIN;
import static no.kantega.Pub.Ingredient.GREEN_STUFF;
import static no.kantega.Pub.Ingredient.GRENADINE;
import static no.kantega.Pub.Ingredient.LIME_JUICE;
import static no.kantega.Pub.Ingredient.RUM;
import static no.kantega.Pub.Ingredient.TONIC_WATER;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class Pub {

    enum Drink {
        HANSA(74),
        GRANS(103),
        STRONGBOW(110),
        GT(new Recipe(GREEN_STUFF, TONIC_WATER, GIN), 2),
        BACARDI_SPECIAL(new Recipe(
                entry(GIN, new BigDecimal("0.5")),
                entry(RUM, new BigDecimal("1.0")),
                entry(GRENADINE, new BigDecimal("1.0")),
                entry(LIME_JUICE, new BigDecimal("1.0"))
        ), 2);

        private static final Map<String, Drink> reverseLookup =
                stream(Drink.values()).collect(toUnmodifiableMap(Drink::drinkName, identity()));

        private static final BigDecimal DISCOUNT = new BigDecimal("0.9");

        private final BigDecimal price;
        private final int maxAmount;
        private final boolean isDiscountable;

        Drink(final int price) {
            this.price = new BigDecimal(price);
            this.maxAmount = Integer.MAX_VALUE;
            this.isDiscountable = true;
        }

        Drink(final Recipe recipe, final int maxAmount) {
            this.price = recipe.price();
            this.maxAmount = maxAmount;
            this.isDiscountable = false;
        }

        String drinkName() {
            return name().toLowerCase();
        }

        BigDecimal price(int units) {
            if (units > maxAmount) {
                throw new TooManyDrinksException(units, this);
            }

            return price.multiply(new BigDecimal(units));
        }

        BigDecimal discountedPrice(int units) {
            final var sumTotal = price(units);
            return isDiscountable ? sumTotal.multiply(DISCOUNT) : sumTotal;
        }

        static Drink byName(final String name) {
//            return Drink.valueOf(name.toUpperCase());
            return Optional.ofNullable(reverseLookup.get(name))
                    .orElseThrow(() -> new NoSuchDrinkException(name));
        }
    }

    enum Ingredient {
        RUM(65), GRENADINE(10), LIME_JUICE(10), GREEN_STUFF(10), TONIC_WATER(20), GIN(85);

        private final BigDecimal unitPrice;

        Ingredient(final int unitPrice) {
            this.unitPrice = new BigDecimal(unitPrice);
        }
    }

    static class Recipe {
        private final Map<Ingredient, BigDecimal> ingredientsToMeasures;

        @SafeVarargs
        Recipe(final Entry<Ingredient, BigDecimal>... ingredientAndMeasureEntries) {
            ingredientsToMeasures = Map.ofEntries(ingredientAndMeasureEntries);
        }

        Recipe(final Ingredient... ingredients) {
            ingredientsToMeasures = stream(ingredients).collect(toUnmodifiableMap(identity(), unused -> ONE));
        }

        BigDecimal price() {
            return ingredientsToMeasures.entrySet().stream()
                    .map(entry -> entry.getKey().unitPrice.multiply(entry.getValue()))
                    .reduce(ZERO, BigDecimal::add);
        }
    }

    static class TooManyDrinksException extends RuntimeException {
        TooManyDrinksException(final int amount, final Drink drink) {
            super(String.format("%d drinks are just too many; the maximum for %s is %d",
                    amount, drink.drinkName(), drink.maxAmount));
        }
    }

    static class NoSuchDrinkException extends RuntimeException {
        NoSuchDrinkException(final String drinkName) {
            super(String.format("No drink with the name of %s exists", drinkName));
        }
    }

    public int computeCost(final String drinkName, final boolean student, final int amount) {
        final var drink = Drink.byName(drinkName);
        final var sumTotal = student ? drink.discountedPrice(amount) : drink.price(amount);
        return roundToClosestInteger(sumTotal);
    }

    private static int roundToClosestInteger(final BigDecimal number) {
        final var numberOfIntegerDigits = number.precision() - number.scale();
        final var toClosestInteger = new MathContext(numberOfIntegerDigits, HALF_UP);
        return number.round(toClosestInteger).intValue();
    }
}