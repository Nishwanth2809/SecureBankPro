# Phase 3 — Collections Framework Study Guide

In this phase, we moved from single-object models to full-fledged in-memory storage using the **Java Collections Framework**. 

Here is a summary of the core concepts we implemented, how they work under the hood, and where to find them in our codebase.

---

## 1. List & ArrayList
An ordered collection (also known as a sequence). Elements can be inserted and accessed by their integer index.
- **Underlying Structure**: Backed by a dynamically resizing array. Accessing elements by index is $O(1)$, but adding or removing elements in the middle requires shifting elements, which is $O(N)$.
- **Implementation**: Used in `InMemoryTransactionRepository` to store all transaction logs chronologically:
  ```java
  private final List<Transaction> transactions = new ArrayList<>();
  ```
  And in `TransactionServiceImpl` to return lists of filtered or sorted transactions.

---

## 2. Map & HashMap
A collection mapping unique keys to values. It cannot contain duplicate keys; each key maps to exactly one value.
- **Underlying Structure**: Backed by a hashtable. It uses key hash codes to place values into buckets. Provides constant-time $O(1)$ performance for basic operations (`get`, `put`, `remove`, `containsKey`).
- **Implementation**: Used in `InMemoryUserRepository` and `InMemoryAccountRepository` for $O(1)$ database-style lookups:
  ```java
  private final Map<Integer, User> usersById = new HashMap<>();
  private final Map<String, User> usersByEmail = new HashMap<>();
  private final Map<String, Account> accountsByNumber = new HashMap<>();
  ```

---

## 3. Set & HashSet
A collection that contains no duplicate elements. It models the mathematical set abstraction.
- **Underlying Structure**: Backed by a `HashMap` instance where elements are stored as map keys with a dummy value. Offers $O(1)$ performance for lookups (`contains`), additions, and removals.
- **Implementation**: Used in `AuthServiceImpl` to keep track of active sessions (logged-in user emails) without duplicates:
  ```java
  private final Set<String> activeSessions = new HashSet<>();
  ```

---

## 4. Queue & LinkedList
A collection designed for holding elements prior to processing. Besides basic Collection operations, queues provide additional insertion, extraction, and inspection operations following FIFO (First-In-First-Out) order.
- **Underlying Structure**: `Queue` is an interface. We used `LinkedList` as the implementation, which is a doubly-linked list.
- **FIFO Operations**:
  - `offer(e)`: Inserts element at the tail of the queue.
  - `peek()`: Inspects the element at the head of the queue without removing it.
  - `poll()`: Retrieves and removes the element at the head of the queue.
- **Implementation**:
  - **Transaction processing**: Queueing pending transactions in `InMemoryTransactionRepository`:
    ```java
    private final Queue<Transaction> pendingQueue = new LinkedList<>();
    ```
  - **Activity Logging**: Restricting login logs to the last 10 events by polling when size exceeds 10 in `AuthServiceImpl`:
    ```java
    sessionActivityLog.offer(event);
    if (sessionActivityLog.size() > 10) {
        sessionActivityLog.poll();
    }
    ```

---

## 5. Iterator
An object that enables traversing through a collection step-by-step.
- **Why use it**: Safe way to read and remove items while iterating (standard `for` loops throw `ConcurrentModificationException` if you attempt to modify the collection during iteration).
- **Implementation**: Used in `InMemoryUserRepository.java` to perform query searches across all stored users:
  ```java
  Iterator<User> iterator = usersById.values().iterator();
  while (iterator.hasNext()) {
      User user = iterator.next();
      if (user.getFullName().toLowerCase().contains(query)) {
          results.add(user);
      }
  }
  ```

---

## 6. Comparable vs Comparator
Both are interfaces used to sort collections of custom objects, but they serve different purposes.

### Comparable (Natural Ordering)
- **Defined**: Inside the entity class itself by implementing `Comparable<T>` and overriding `compareTo(T o)`.
- **Purpose**: Establishes the "natural" default sort order.
- **Implementation**: `Transaction` implements `Comparable<Transaction>` to sort naturally by transaction ID ascending:
  ```java
  public class Transaction implements Comparable<Transaction> {
      @Override
      public int compareTo(Transaction other) {
          return Integer.compare(this.transactionId, other.transactionId);
      }
  }
  ```
  Sorted using: `Collections.sort(transactionList);`

### Comparator (Custom Ordering)
- **Defined**: Externally as separate classes or static instances by implementing `Comparator<T>` and overriding `compare(T o1, T o2)`.
- **Purpose**: Establishes alternative, custom sort orders (e.g. by amount, by date).
- **Implementation**: `Transaction` declares static Comparators:
  ```java
  public static final Comparator<Transaction> BY_AMOUNT_ASC = Comparator.comparing(Transaction::getAmount);
  public static final Comparator<Transaction> BY_AMOUNT_DESC = BY_AMOUNT_ASC.reversed();
  ```
  Sorted using: `transactionList.sort(Transaction.BY_AMOUNT_DESC);`
