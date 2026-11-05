/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.NotNull;
import java.lang.constant.ConstantDesc;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Sealed types in object generation")
class ObjectGeneratorSealedTypeTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    /** Listed in reverse name order, so the engine's sorted order differs from the JVM's. */
    sealed interface Shape permits Zed, Alpha {}

    record Zed(String label) implements Shape {}

    record Alpha(int size) implements Shape {}

    record Drawing(Shape shape) {}

    record Gallery(List<Shape> shapes) {}

    /** A nested sealed interface, an enum, and a concrete sealed class among the subclasses. */
    sealed interface Vehicle permits Car, Truck, Signal, Bus {}

    record Car(String model) implements Vehicle {}

    sealed interface Truck extends Vehicle permits Pickup, Lorry {}

    record Pickup(int payload) implements Truck {}

    record Lorry(int axles) implements Truck {}

    enum Signal implements Vehicle {
        STOP,
        GO
    }

    static sealed class Bus implements Vehicle permits DoubleDecker {
        int seats;
    }

    static final class DoubleDecker extends Bus {
        int decks;
    }

    static class Fleet {
        Vehicle vehicle;
        Bus bus;
    }

    abstract static sealed class Account permits Savings, Checking {
        String owner;
    }

    static final class Savings extends Account {
        double rate;
    }

    static final class Checking extends Account {
        int overdraft;
    }

    static class Bank {
        Account account;
    }

    sealed interface Payment permits Card, Custom {}

    record Card(String number) implements Payment {}

    non-sealed interface Custom extends Payment {}

    record Wallet(String id) implements Custom {}

    static class Checkout {
        Payment payment;
    }

    sealed interface Expr permits Num, Add {}

    record Num(int value) implements Expr {}

    record Add(Expr left, Expr right) implements Expr {}

    record Formula(Expr expr) {}

    sealed interface Chain permits Link {}

    record Link(Chain next) implements Chain {}

    record ChainHolder(Chain chain) {}

    sealed interface Command permits Move, Halt {}

    record Move(int distance) implements Command {}

    enum Halt implements Command {
        STOP
    }

    /** The enum sorts first, so the first choice takes it. */
    sealed interface Switch permits Knob, Flip {}

    record Knob(int position) implements Switch {}

    enum Flip implements Switch {
        UP,
        DOWN
    }

    /** Required children, so a child cut at the depth limit fails. */
    sealed interface Term permits Lit, Sum {}

    record Lit(int value) implements Term {}

    record Sum(@NotNull Term left, @NotNull Term right) implements Term {}

    sealed interface Hollow permits Nothing {}

    enum Nothing implements Hollow {}

    record HollowHolder(Hollow hollow) {}

    sealed interface Root permits Left, Right {}

    sealed interface Left extends Root permits Both {}

    sealed interface Right extends Root permits Both {}

    record Both(int value) implements Left, Right {}

    record RootHolder(Root root) {}

    sealed interface Result<T> permits Ok, Err, Fixed {}

    record Ok<T>(T value) implements Result<T> {}

    record Err<T>(String message) implements Result<T> {}

    record Fixed(String text) implements Result<String> {}

    record Response(Result<String> result) {}

    static class Holder<A> {
        A item;
    }

    static class Middle<B> extends Holder<B> {}

    /** Its {@code A} is bound to {@code B}, and {@code B} to {@code Result<String>}. */
    static final class ChainRoot extends Middle<Result<String>> {}

    sealed interface Pair<A, B> permits Same {}

    record Same<T>(T first, T second) implements Pair<T, T> {}

    record PairHolder(Pair<String, String> pair) {}

    sealed interface Source<X> permits Stream {}

    sealed interface Stream<Y> extends Source<Y> permits Pipe {}

    record Pipe<Z>(Z item) implements Stream<Z> {}

    record Plant(Source<Integer> source) {}

    interface Box<T> {}

    record ListBox<T>(List<T> items) implements Box<T> {}

    record Crate(Box<Integer> box) {}

    record DateBox<D extends Date>(D value) implements Box<D> {}

    record Ledger(Box<Timestamp> box) {}

    interface Plain {}

    record PlainImpl(String value) implements Plain {}

    static class Base {
        String name;
    }

    static class Derived extends Base {}

    record Top(Mid mid) {}

    record Mid(@NotNull Shape shape) {}

    record JdkSealedHolder(ConstantDesc constant) {}

    interface AdapterSealed {}

    record AdapterLeaf(String name) implements AdapterSealed {}

    interface AdapterEmpty {}

    interface AdapterUnrelated {}

    interface AdapterSelf {}

    sealed interface AdapterJvmSealed permits AdapterJvmLeaf {}

    record AdapterJvmLeaf(String name) implements AdapterJvmSealed {}

    record AdapterHolder(AdapterSealed sealed) {}

    record AdapterJvmHolder(AdapterJvmSealed sealed) {}

    record AdapterEmptyHolder(AdapterEmpty empty) {}

    record AdapterUnrelatedHolder(AdapterUnrelated unrelated) {}

    record AdapterSelfHolder(AdapterSelf self) {}

    /** Picks the first option of every bounded choice, so the order a choice draws from shows. */
    static final class FirstChoiceRandom extends Random {

        private static final long serialVersionUID = 1L;

        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).build();
    }

    private static Set<Class<?>> classesOf(List<?> values) {
        Set<Class<?>> classes = new HashSet<>();
        values.forEach(value -> classes.add(value.getClass()));
        return classes;
    }

    // ── Choice ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a sealed member takes each concrete subclass across seeds")
    void sealedMemberTakesEachSubclass() {
        List<Shape> shapes = new ArrayList<>();
        for (long seed = 0; seed < 40; seed++) {
            shapes.add(new ObjectGenerator<>(Drawing.class, seeded(seed))
                    .generate()
                    .shape());
        }

        assertEquals(Set.of(Alpha.class, Zed.class), classesOf(shapes));
        shapes.stream()
                .filter(Zed.class::isInstance)
                .forEach(shape -> assertNotNull(((Zed) shape).label(), "a chosen class is fully populated"));
    }

    @Test
    @DisplayName("the same seed chooses the same subclasses")
    void sameSeedChoosesSameSubclasses() {
        List<Drawing> first = new ObjectGenerator<>(Drawing.class, seeded(7L)).generateList(30);
        List<Drawing> second = new ObjectGenerator<>(Drawing.class, seeded(7L)).generateList(30);

        assertEquals(first, second);
        assertEquals(
                Set.of(Alpha.class, Zed.class),
                classesOf(first.stream().map(Drawing::shape).toList()));
    }

    @Test
    @DisplayName("subclasses are drawn in binary-name order, not in the order the type permits them")
    void subclassesAreDrawnInNameOrder() {
        GeneratorConfig firstChoice =
                GeneratorConfig.builder().random(new FirstChoiceRandom()).build();

        assertInstanceOf(
                Alpha.class,
                new ObjectGenerator<>(Drawing.class, firstChoice).generate().shape());
        assertInstanceOf(Alpha.class, new ObjectGenerator<>(Shape.class, firstChoice).generate());
    }

    @Test
    @DisplayName("collection elements of a sealed type each take a subclass")
    void collectionElementsTakeSubclasses() {
        Gallery gallery = new ObjectGenerator<>(
                        Gallery.class,
                        GeneratorConfig.builder()
                                .seed(3L)
                                .collectionSize(30, 30)
                                .build())
                .generate();

        assertEquals(30, gallery.shapes().size());
        assertEquals(Set.of(Alpha.class, Zed.class), classesOf(gallery.shapes()));
    }

    @Test
    @DisplayName("nested sealed interfaces, enums and concrete sealed classes join the choice")
    void nestedHierarchyJoinsTheChoice() {
        List<Vehicle> vehicles = new ArrayList<>();
        Set<Object> signals = new HashSet<>();
        for (long seed = 0; seed < 120; seed++) {
            Fleet fleet = new ObjectGenerator<>(Fleet.class, seeded(seed)).generate();
            vehicles.add(fleet.vehicle);
            if (fleet.vehicle instanceof Signal signal) {
                signals.add(signal);
            }
            assertSame(Bus.class, fleet.bus.getClass(), "a concrete sealed type is generated as itself");
        }

        assertEquals(
                Set.of(Bus.class, Car.class, Lorry.class, Pickup.class, Signal.class),
                classesOf(vehicles),
                "a concrete sealed subclass is a leaf, so DoubleDecker is never chosen");
        assertEquals(Set.of(Signal.STOP, Signal.GO), signals);
    }

    @Test
    @DisplayName("an abstract sealed class takes a subclass with its inherited fields populated")
    void abstractSealedClassTakesSubclass() {
        Set<Class<?>> classes = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) {
            Account account = new ObjectGenerator<>(Bank.class, seeded(seed)).generate().account;
            classes.add(account.getClass());
            assertNotNull(account.owner);
        }

        assertEquals(Set.of(Savings.class, Checking.class), classes);
    }

    @Test
    @DisplayName("a subclass reached through two sealed parents is one choice")
    void diamondIsOneChoice() {
        RootHolder holder = new ObjectGenerator<>(RootHolder.class, seeded(1L)).generate();

        assertInstanceOf(Both.class, holder.root());
    }

    @Test
    @DisplayName("an enum without constants adds no choice, and a type left with none fails")
    void enumWithoutConstantsAddsNoChoice() {
        ObjectGenerationException failure = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(HollowHolder.class, seeded(1L)).generate());

        GenerationFailureContext context = failure.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("HollowHolder.hollow", context.path());
        assertEquals(
                "Sealed type " + Hollow.class.getName()
                        + " has no subclass to generate; register a type override for it",
                failure.getCause().getMessage());
    }

    // ── Cycles ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a recursive hierarchy ends in a leaf instead of a cut cycle")
    void recursiveHierarchyEndsInLeaf() {
        Set<Class<?>> roots = new HashSet<>();
        for (long seed = 0; seed < 200; seed++) {
            Expr expr = new ObjectGenerator<>(Formula.class, seeded(seed))
                    .generate()
                    .expr();
            roots.add(expr.getClass());
            assertNoNulls(expr);
        }

        assertEquals(Set.of(Num.class, Add.class), roots);
    }

    private static void assertNoNulls(Expr expr) {
        assertNotNull(expr, "a cycle was cut");
        if (expr instanceof Add add) {
            assertNoNulls(add.left());
            assertNoNulls(add.right());
        }
    }

    @Test
    @DisplayName("a hierarchy with only recursive subclasses keeps the cycle guard")
    void onlyRecursiveSubclassesKeepCycleGuard() {
        ChainHolder holder = new ObjectGenerator<>(ChainHolder.class, seeded(5L)).generate();

        Link link = assertInstanceOf(Link.class, holder.chain());
        assertNull(link.next(), "the repeated Link is cut as any cycle is");
    }

    // ── Precedence ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a type override or objectSubtype mapping for the declared sealed type wins")
    void declaredTypeOverrideAndMappingWin() {
        GeneratorConfig overridden = GeneratorConfig.builder()
                .seed(1L)
                .objectOverride(Shape.class, () -> new Zed("fixed"))
                .build();
        GeneratorConfig mapped = GeneratorConfig.builder()
                .seed(1L)
                .objectSubtype(Shape.class, Zed.class)
                .build();

        for (Drawing drawing : new ObjectGenerator<>(Drawing.class, overridden).generateList(10)) {
            assertEquals(new Zed("fixed"), drawing.shape());
        }
        for (Drawing drawing : new ObjectGenerator<>(Drawing.class, mapped).generateList(10)) {
            assertInstanceOf(Zed.class, drawing.shape());
        }
        assertEquals(new Zed("fixed"), new ObjectGenerator<>(Shape.class, overridden).generate());
        for (Shape shape : new ObjectGenerator<>(Shape.class, mapped).generateList(10)) {
            assertInstanceOf(Zed.class, shape);
        }
    }

    @Test
    @DisplayName("a type override for a chosen subclass generates it, with the member's context")
    void chosenSubclassOverrideGeneratesIt() {
        GeneratorConfig plain = GeneratorConfig.builder()
                .seed(2L)
                .objectOverride(Alpha.class, () -> new Alpha(7))
                .build();
        List<String> fieldNames = new ArrayList<>();
        ContextualGenerator<Alpha> contextual = context -> {
            fieldNames.add(context.getFieldName());
            return new Alpha(context.getDepth() + 100);
        };
        GeneratorConfig withContext = GeneratorConfig.builder()
                .seed(2L)
                .objectOverride(Alpha.class, contextual)
                .build();

        Set<Class<?>> classes = new HashSet<>();
        for (Drawing drawing : new ObjectGenerator<>(Drawing.class, plain).generateList(30)) {
            classes.add(drawing.shape().getClass());
            if (drawing.shape() instanceof Alpha alpha) {
                assertEquals(7, alpha.size());
            }
        }
        for (Drawing drawing : new ObjectGenerator<>(Drawing.class, withContext).generateList(30)) {
            if (drawing.shape() instanceof Alpha alpha) {
                assertEquals(100, alpha.size());
            }
        }

        assertEquals(Set.of(Alpha.class, Zed.class), classes);
        assertTrue(!fieldNames.isEmpty() && fieldNames.stream().allMatch("shape"::equals));
    }

    // ── Subclasses that cannot be built ──────────────────────────────────────

    @Test
    @DisplayName("an abstract subclass that is not sealed fails whatever the seed")
    void abstractUnsealedSubclassFails() {
        for (long seed = 0; seed < 5; seed++) {
            long chosenSeed = seed;
            ObjectGenerationException failure = assertThrows(
                    ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(Checkout.class, seeded(chosenSeed)).generate());

            GenerationFailureContext context = failure.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals("Checkout.payment", context.path());
            assertEquals(
                    "Sealed type " + Payment.class.getName() + " permits " + Custom.class.getName()
                            + ", which is abstract and not sealed; map it with objectSubtype or register a type"
                            + " override for it",
                    failure.getCause().getMessage());
        }
    }

    @Test
    @DisplayName("lenient mode leaves a member with an unbuildable subclass null and tells the listener")
    void lenientModeLeavesUnbuildableMemberNull() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(1L)
                .objectIgnoreErrors(true)
                .generationFailureListener(diagnostics::add)
                .build();

        Checkout checkout = new ObjectGenerator<>(Checkout.class, config).generate();

        assertNull(checkout.payment);
        assertEquals("Checkout.payment", diagnostics.getFirst().context().path());
    }

    @Test
    @DisplayName("an objectSubtype mapping or a type override makes an abstract subclass a choice")
    void mappingOrOverrideMakesAbstractSubclassAChoice() {
        GeneratorConfig.Builder mapped = GeneratorConfig.builder().objectSubtype(Custom.class, Wallet.class);
        GeneratorConfig.Builder overridden =
                GeneratorConfig.builder().objectOverride(Custom.class, () -> new Wallet("override"));

        Set<Class<?>> mappedClasses = new HashSet<>();
        Set<Object> overriddenValues = new HashSet<>();
        for (long seed = 0; seed < 30; seed++) {
            mappedClasses.add(
                    new ObjectGenerator<>(Checkout.class, mapped.seed(seed).build())
                            .generate()
                            .payment
                            .getClass());
            Payment payment =
                    new ObjectGenerator<>(Checkout.class, overridden.seed(seed).build()).generate().payment;
            overriddenValues.add(payment instanceof Wallet ? payment : Card.class);
        }

        assertEquals(Set.of(Card.class, Wallet.class), mappedClasses);
        assertEquals(Set.of(Card.class, new Wallet("override")), overriddenValues);
    }

    @Test
    @DisplayName("a required sealed member cut at objectMaxDepth asks for more depth")
    void requiredSealedMemberAtMaxDepthAsksForDepth() {
        ObjectGenerationException failure = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(
                                Top.class,
                                GeneratorConfig.builder().objectMaxDepth(1).build())
                        .generate());

        assertEquals("Top.mid.shape", failure.getContext().orElseThrow().path());
        assertEquals(
                "required member would be null at objectMaxDepth 1; raise objectMaxDepth or override the member",
                failure.getCause().getMessage());
    }

    @Test
    @DisplayName("a sealed JDK type is not generated reflectively")
    void sealedJdkTypeIsUnsupported() {
        ObjectGenerationException failure = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(JdkSealedHolder.class, seeded(1L)).generate());

        assertEquals(
                "No generator is registered for the declared type",
                failure.getCause().getMessage());
    }

    // ── Roots ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a sealed root is generated as the chosen subclass would be from the same seed")
    void sealedRootIsGeneratedAsChosenSubclass() {
        Set<Class<?>> classes = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) {
            GeneratorConfig config = seeded(seed);
            Shape shape = new ObjectGenerator<>(Shape.class, config).generate();
            classes.add(shape.getClass());

            assertEquals(new ObjectGenerator<>(shape.getClass(), config).generate(), shape);
        }

        assertEquals(Set.of(Alpha.class, Zed.class), classes);
        assertEquals(
                new ObjectGenerator<>(Shape.class, seeded(9L)).generateList(10),
                new ObjectGenerator<>(Shape.class, seeded(9L)).generateList(10));
    }

    @Test
    @DisplayName("an unseeded sealed root takes a subclass")
    void unseededSealedRootTakesSubclass() {
        assertInstanceOf(Shape.class, new ObjectGenerator<>(Shape.class).generate());
    }

    @Test
    @DisplayName("a sealed root can be one of an enum's constants")
    void sealedRootCanBeEnumConstant() {
        GeneratorConfig firstChoice =
                GeneratorConfig.builder().random(new FirstChoiceRandom()).build();
        Set<Object> commands = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) {
            Command command = new ObjectGenerator<>(Command.class, seeded(seed)).generate();
            commands.add(command instanceof Halt ? command : Move.class);
        }

        assertSame(Halt.STOP, new ObjectGenerator<>(Command.class, firstChoice).generate());
        assertEquals(Set.of(Halt.STOP, Move.class), commands);
    }

    @Test
    @DisplayName("a type override for an enum chosen at a sealed root generates it")
    void enumChosenAtRootTakesItsOverride() {
        ContextualGenerator<Flip> contextual = context -> Flip.DOWN;
        GeneratorConfig plain = GeneratorConfig.builder()
                .random(new FirstChoiceRandom())
                .objectOverride(Flip.class, () -> Flip.DOWN)
                .build();
        GeneratorConfig withContext = GeneratorConfig.builder()
                .random(new FirstChoiceRandom())
                .objectOverride(Flip.class, contextual)
                .build();
        GeneratorConfig none =
                GeneratorConfig.builder().random(new FirstChoiceRandom()).build();

        assertSame(Flip.UP, new ObjectGenerator<>(Switch.class, none).generate());
        assertSame(Flip.DOWN, new ObjectGenerator<>(Switch.class, plain).generate());
        assertSame(Flip.DOWN, new ObjectGenerator<>(Switch.class, withContext).generate());
    }

    @Test
    @DisplayName("the class chosen at a sealed root counts as being generated, so its members end in a leaf")
    void classChosenAtRootCountsAsBeingGenerated() {
        GeneratorConfig.Builder shallow = GeneratorConfig.builder().objectMaxDepth(1);
        Set<Class<?>> roots = new HashSet<>();
        for (long seed = 0; seed < 60; seed++) {
            Term term = new ObjectGenerator<>(Term.class, shallow.seed(seed).build()).generate();
            roots.add(term.getClass());
            if (term instanceof Sum sum) {
                assertInstanceOf(Lit.class, sum.left());
                assertInstanceOf(Lit.class, sum.right());
            }
        }

        assertEquals(Set.of(Lit.class, Sum.class), roots);
    }

    @Test
    @DisplayName("a sealed root with a subclass that cannot be built fails with context")
    void sealedRootWithUnbuildableSubclassFails() {
        ObjectGenerationException failure = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(Payment.class, seeded(1L)).generate());

        GenerationFailureContext context = failure.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("Payment", context.path());
        assertEquals(Payment.class.getTypeName(), context.declaredType());
        assertEquals(
                "Unsupported type at 'Payment' (declared type " + Payment.class.getTypeName() + ", depth 0)",
                failure.getMessage());
    }

    @Test
    @DisplayName("an interface or abstract root takes its objectSubtype mapping, a concrete root does not")
    void abstractRootTakesItsMapping() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(1L)
                .objectSubtype(Plain.class, PlainImpl.class)
                .objectSubtype(Base.class, Derived.class)
                .build();

        assertInstanceOf(PlainImpl.class, new ObjectGenerator<>(Plain.class, config).generate());
        assertSame(
                Base.class, new ObjectGenerator<>(Base.class, config).generate().getClass());
    }

    // ── Generic hierarchies ───────────────────────────────────────────────────

    @Test
    @DisplayName("a generic sealed member reached through inherited type variables passes its arguments")
    void genericSealedMemberThroughVariableChainPassesArguments() {
        Set<Class<?>> classes = new HashSet<>();
        for (long seed = 0; seed < 40; seed++) {
            Object item = new ObjectGenerator<>(ChainRoot.class, seeded(seed)).generate().item;
            classes.add(item.getClass());
            if (item instanceof Ok<?> ok) {
                assertInstanceOf(String.class, ok.value());
            }
        }

        assertEquals(Set.of(Ok.class, Err.class, Fixed.class), classes);
    }

    @Test
    @DisplayName("a generic sealed member passes its type arguments to the chosen subclass")
    void genericSealedMemberPassesArguments() {
        Set<Class<?>> classes = new HashSet<>();
        for (long seed = 0; seed < 40; seed++) {
            Result<String> result = new ObjectGenerator<>(Response.class, seeded(seed))
                    .generate()
                    .result();
            classes.add(result.getClass());
            if (result instanceof Ok<String> ok) {
                assertInstanceOf(String.class, ok.value());
            }
        }

        assertEquals(Set.of(Ok.class, Err.class, Fixed.class), classes);
    }

    @Test
    @DisplayName("type arguments reach a subclass through every generic parent in between")
    void typeArgumentsReachThroughParents() {
        Pipe<?> pipe = assertInstanceOf(
                Pipe.class,
                new ObjectGenerator<>(Plant.class, seeded(1L)).generate().source());
        Same<?> same = assertInstanceOf(
                Same.class,
                new ObjectGenerator<>(PairHolder.class, seeded(1L)).generate().pair());

        assertInstanceOf(Integer.class, pipe.item());
        assertInstanceOf(String.class, same.first());
        assertInstanceOf(String.class, same.second());
    }

    @Test
    @DisplayName("a generic objectSubtype mapping takes the declared type arguments")
    void genericMappingTakesDeclaredArguments() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(1L)
                .objectSubtype(Box.class, ListBox.class)
                .build();

        ListBox<?> box = assertInstanceOf(
                ListBox.class,
                new ObjectGenerator<>(Crate.class, config).generate().box());

        assertTrue(!box.items().isEmpty());
        box.items().forEach(item -> assertInstanceOf(Integer.class, item));
    }

    @Test
    @DisplayName("a bounded type variable of a mapped implementation takes the declared argument, not its bound")
    void boundedMappingTakesDeclaredArgument() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(1L)
                .objectSubtype(Box.class, DateBox.class)
                .build();

        DateBox<?> box = assertInstanceOf(
                DateBox.class,
                new ObjectGenerator<>(Ledger.class, config).generate().box());

        assertInstanceOf(Timestamp.class, box.value(), "the bound would give a plain Date");
    }

    // ── Construction adapters ─────────────────────────────────────────────────

    @Test
    @DisplayName("a construction adapter can declare a type sealed and list its subclasses")
    void adapterDeclaresSubclasses() {
        assertInstanceOf(
                AdapterLeaf.class,
                new ObjectGenerator<>(AdapterHolder.class, seeded(1L))
                        .generate()
                        .sealed());
        assertInstanceOf(
                AdapterJvmLeaf.class,
                new ObjectGenerator<>(AdapterJvmHolder.class, seeded(1L))
                        .generate()
                        .sealed(),
                "an adapter that declares nothing leaves the JVM's permitted subclasses");
    }

    @Test
    @DisplayName("an adapter's sealed type without subclasses fails")
    void adapterSealedTypeWithoutSubclassesFails() {
        ObjectGenerationException failure = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(AdapterEmptyHolder.class, seeded(1L)).generate());

        assertEquals(
                "Sealed type " + AdapterEmpty.class.getName()
                        + " has no subclass to generate; register a type override for it",
                failure.getCause().getMessage());
    }

    @Test
    @DisplayName("an adapter that lists a class outside the type fails with context")
    void adapterListingForeignClassFails() {
        ObjectGenerationException unrelated = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(AdapterUnrelatedHolder.class, seeded(1L)).generate());
        ObjectGenerationException self = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(AdapterSelfHolder.class, seeded(1L)).generate());

        assertEquals(
                "AdapterUnrelatedHolder.unrelated",
                unrelated.getContext().orElseThrow().path());
        assertEquals(
                "Sealed type " + AdapterUnrelated.class.getName() + " lists " + AdapterLeaf.class.getName()
                        + ", which is not a subclass of it",
                unrelated.getCause().getMessage());
        assertEquals(
                "Sealed type " + AdapterSelf.class.getName() + " lists " + AdapterSelf.class.getName()
                        + ", which is not a subclass of it",
                self.getCause().getMessage());
    }

    @Test
    @DisplayName("an adapter declares no sealed type unless it says so")
    void adapterDeclaresNoSealedTypeByDefault() {
        ObjectConstructionAdapter adapter = new ObjectConstructionAdapter() {
            @Override
            public boolean supports(Class<?> type) {
                return false;
            }

            @Override
            public Object construct(ObjectConstructionContext<?> context) {
                throw new UnsupportedOperationException();
            }
        };

        assertEquals(Optional.empty(), adapter.sealedSubclasses(Shape.class));
    }
}
