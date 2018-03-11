# Debouncer

**Debouncer** is a small library intended for debouncing rapid events. 

A debouncer is made up of three parts.

- [_Accumulation_](#accumulators) - Accumulate events into some type of result.
- [_Regulation_](#regulators) - Regulate the throughput of events.
- [_Consumption_](#consumers) - Consumption of the events.

### <a name="accumulators"/> Accumulators
Accumulators accumulate value types into a result type. 

|Accumulator | Description | Value Type | Result Type | Purpose|
|----------- | ----------- | ---------- | ----------- | -------|
|<a name="LatestValueAccumulator"/>`LatestValueAccumulator` | Only keeps the last value accumulated. | `T` | `T` | User is typing, but only need the final value entered.|
|<a name="ListAccumulator"/>`ListAccumulator` | Keeps a list of value types accumulated | `T` | `List<T>` | Rapid events from an external source. All events need to be shown, but multiple updates would cause flickering.|
|<a name="MapAccumulator"/>`MapAccumulator` | Keeps a map of value types to sub-accumulators. | `V` | `Map<K, Accumulator<V, R>>`| Rapid events coming in from an external source. Each event has a key that it can be grouped by. The resulting groupings can then be accumulated by one of the other accumulators. (See [Drainers.drainMap](#drainMap))|

An Accumulator decorator is also included, with the following implementation:

|Decorator | Usage|
|--------- | -----|
|<a name="SynchronizedAccumulator"/>`SynchronizedAccumulator` | Used to wrap an accumulator's calls in `synchronized` blocks.|

### <a name="regulators"/> Regulators
Regulators regulate the flow of events. Requests to consume the accumulator is made, then the regulator determines when to execute the drain.

|Executor | Description | Purpose|
|-------- | ----------- | -------|
|<a name="ImmediateRegulator"/>`ImmediateRegulator` | Immediately executes events. | Mostly included for testing purposes.|
|<a name="CountingRegulator"/>`CountingRegulator` | Waits until a certain number of events have been accumulated, then runs once. | Could be used for buffering a large number of events coming in, but not wanting to get overloaded.|
|<a name="DelayedRegulator"/>`DelayedRegulator` | Waits a given delay after the first event, or first event after delay, ignores other events. | Rapid events coming in, but want to update on a schedule to avoid flickering.|

### <a name="consumers"/> Consumers/Drainers
Consumers use the `java.util.function.Consumer` class. Several convenience methods are included in the `Drainers` utility class.

|`Drainers` Method | Arguments | Purpose|
|----------------- | --------- | -------|
|<a name="noopDrainer"/>`noopDrainer`     | *none*    | Does nothing during drain. Used primarily for testing accumulators. Can be used to clear an accumulator. |
|<a name="drainIterable"/>`drainIterable`   | `Consumer<T>` | Used to drain an accumulator whose result type extends `Iterable`. Drains each value into the provided consumer.|
|<a name="drainMap"/>`drainMap`        | `BiConsumer<K, R>` | Used to drain a `MapAccumulator` by key and result type. Drains each key and the result of each associated sub-accumulator into the provided bi-function.|`Consumer<K, R>` | Used to drain a `MapAccumulator` by key and result type. Drains each key and the result of each associated sub-accumulator into the provided bi-function.|