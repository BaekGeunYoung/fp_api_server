# functional polymorphic programming using kotlin & arrow

kotlin + spring webflux + arrow 를 이용해 functional polymorphic한 architecture로 http server를 구축해보는 실습 코드입니다.

## Reactor & Monad

spring webflux가 사용하고 있는 reactor framework는 reactive stream API를 구현한 구현체 중 하나이다. reactor에서 제공하는 Mono와 Flux를 이용하면 non-blocking io를 기반으로 높은 동시성을 가진 어플리케이션을 작성할 수 있다.

Mono와 Flux를 이용해 어플리케이션을 작성하면 map과 flatMap을 굉장히 많이 쓰게 되는데, 이 flatMap은 FP에서 굉장히 중요하게 다뤄지는 Monad를 구성하는 기본 수단이며, 따라서 Mono와 Flux도 이 Monad의 한 instance이다.

## Mono 일반화하기

reactor framework는 단지 reactive stream API를 구현한 구현체이기 때문에, reactor에서 제공하는 Mono라는 녀석도 Reactor 만의 특별한 의미를 담고 있는 것은 아니다. Mono라는 클래스의 의미를 일반화해서 생각해보면 아래 문장 정도로 정리해볼 수 있을 것 같다.

`Mono<T>` is a `T를 (어딘가에서) 비동기적으로 가져올 수 있게 하는 것`

실제로 이 Mono와 같은 역할을 하는 녀석이 Rx에도 있고(`Observable`), akka streams에도 있다. (`sink, source, flow`) 그리고 reactive programming 방식은 아니지만 Java에서도 언어 레벨에서 기본적으로 비동기 IO 연산을 가능하게끔 해주는 `Future`라는 클래스가 존재한다.
 
그리고 추상화 수준을 한 단계 높여서 Mono를 아래와 같이 표현해볼 수도 있다.

`Mono<T>` is a `T를 (어딘가에서) 가져올 수 있게 하는 것`

이렇게 추상화하게 되면 비동기 IO를 지원하지 않는 단순한 `IO` 클래스 같은 녀석들도 같은 추상화로 묶일 수 있다.

## functional polymorphic programming

이렇게 어플리케이션 바깥의 외부 세계와 소통함으로써 데이터를 가져오는 녀석들을 우리는 어떤 `F`로 추상화할 수 있다. 그리고 이 F가 FP의 typeclass 중 하나라면(특히 Monad라면) typeclass에서 제공하는 "일반적인" 메소드들만을 이용하여 polymorphic programming을 할 수 있게 된다.

실제로 Mono를 이용해 프로그래밍을 할 때, 우리는 Mono에 대해 하나도 알지 못하고 이 녀석이 Monad라는 것만 알아도 로직을 작성하는 데 큰 문제가 없다. 이렇게 typeclass에 대해 일반적인 프로그래밍을 하는 것을 "functional polymorphic programming"이라고 이름붙였으며 이렇게 했을 때 내가 생각하는 장점은 아래와 같다.

1. 비즈니스 레이어를 최대한 외부 의존성 없이 순수하게 유지함으로써 확장성을 갖출 수 있다. 만약 Reactor가 아닌 Rx를 쓰게된다고 하면 의존성을 주입해주는 파일 하나만 바꿔주면 됨

2. 테스트 용이성. 기존처럼 Mono에 의존한 코드를 작성하게 되면 테스트 코드는 어느순간 mono의 동작과 비즈니스 로직을 동시에 테스트하고 있게 되어버림. mono를 철저히 모른 상태에서 비즈니스 레이어를 만들면 테스트도 자연스럽게 로직에 대한 테스트만 수행할 수 있게 된다.

이 추상화를 코드 레벨에서 실현 가능하게 해주는 라이브러리가 필요한데, kotlin에는 arrow라는 라이브러리가 존재한다. 지금부터 arrow를 이용해 functional polymorphic programming을 하는 방법에 대해 알아보자.

## higher kinded type using Arrow

위에서 설명했듯이 polymorphic programming을 하려면 `Mono<T>`, `Observable<T>`, `Future<T>` 같은 구체적인 클래스를 어떤 추상화된 `F<T>`로 표현할 수 있어야 한다. 이를 할 수 있게 해주는 것이 바로 `higher kinded type`이다. 하지만 kotlin은 이것을 native syntax로 지원하지 않고 있고, arrow에서 workaround한 방식으로 이를 구현해놓았다.

```kotlin
interface Kind<F, A> // == F<A>
```

이를 이용해 Mono가 포함된 함수의 signature를 Mono를 알지 못하게 "일반적으로" 바꿀 수 있다.

```kotlin
// change this
fun <A, B> funA(a: Mono<A>): Mono<B>

// into
fun <F, A, B> funA(a: Kind<F, A>): Kind<F, B>
```

## Repository interface 작성하기

아주 쉽다.

```kotlin
interface UserRepository<F> {
    fun findById(id: Long): Kind<F, Optional<User>>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
}
```

## "추상화된" Repository implementation 작성하기

대게 추상화는 interface가 담당하는 영역이지만, repository의 구현체도 "외부 effect에 대해" 추상화된 채 구현되어야 한다는 것을 명심하자.

보통 Reactor + JPA 조합으로 Repository를 구현할 땐 JDBC가 blocking operation만을 제공하기 때문에 동시성을 유지하기 위해서 context switching을 필연적으로 발생시켜야 한다. reactor에서 이 역할을 해주는 함수가 `subscribeOn`, `publishOn` 등인데, 이 기능을 일반화환 FP typeclass로 `Async`라는 녀석이 있다.

```kotlin
class UserRepositoryImpl<F>(
    private val A: Async<F>,
    private val userJpaRepository: UserJpaRepository
) : UserRepository<F>, Async<F> by A {
    private val ioDispatcher =
        Schedulers.newBoundedElastic(400, 100, "db")
            .asCoroutineDispatcher()

    override fun findById(id: Long): Kind<F, Optional<User>> =
        later(ioDispatcher) { userJpaRepository.findById(id) }

    override fun findAll(): Kind<F, List<User>> =
        later(ioDispatcher) { userJpaRepository.findAll() }

    override fun delete(id: Long): Kind<F, Unit> =
        later(ioDispatcher) { userJpaRepository.deleteById(id) }

    override fun update(user: User): Kind<F, Unit> =
        later(ioDispatcher) { userJpaRepository.save(user) }

    override fun insert(user: User): Kind<F, Unit> =
        later(ioDispatcher) { userJpaRepository.save(user) }
}

interface UserJpaRepository : JpaRepository<User, Long>
```

constructor의 parameter로 `A: Async<F>`를 받고, kotlin delegate pattern을 이용해 `later`라는 함수를 바로 사용할 수 있도록 했다.

## Service interface 작성하기

아주 쉽다.

```kotlin
interface UserService<F> {
    fun findById(id: Long): Kind<F, User>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
    fun findAllAndUpdate(): Kind<F, Unit>
}
```

주목할 것은 `findAllAndUpdate` 함수인데, "모든 엔티티를 한 번 가져온 후, 특정 필드를 update하는 작업을 각각의 엔티티에 대해 concurrent하게 수행하는 함수"를 작성해보려 한다.

비슷한 방식으로, 이번엔 `Async`가 아니라 `Monad`를 주입받아서 구현체를 작성한다.

```kotlin
class UserServiceImpl<F> (
    private val M: Monad<F>,
    private val CM: ConcurrentMappable<F>,
    private val userRepository: UserRepository<F>,
) : UserService<F>, Monad<F> by M {
    override fun findById(id: Long): Kind<F, User> =
        userRepository.findById(id).map {
            if (it.isPresent) it.get()
            else throw EntityNotFoundException(User::class, id)
        }

    override fun findAll(): Kind<F, List<User>> =
        userRepository.findAll()

    override fun delete(id: Long): Kind<F, Unit> =
        findById(id).flatMap {
            userRepository.delete(id)
        }

    override fun update(user: User): Kind<F, Unit> =
        findById(user.id).flatMap {
            userRepository.update(user)
        }

    override fun insert(user: User): Kind<F, Unit> =
        userRepository.insert(user)

    override fun findAllAndUpdate(): Kind<F, Unit> = CM.run {
        findAll()
            .concurrentMap {
                val updated = it.copy(name = "fake name")
                userRepository.update(updated)
            }
            .map { Unit }
    }
}
```

다른 함수들은 별로 특별할 것이 없고, `findAllAndUpdate` 함수를 살펴보자.

`concurrentMap`이라는 함수를 이용했고, 이 함수는 constructor에서 두 번째 parameter로 주입받은 `ConcurrentMappable`이라는 typeclass가 제공하는 함수이다. concurrentMap 함수의 siganature는 다음과 같다.

```kotlin
fun <F, A, B> Kind<F, List<A>>.concurrentMap(f: (A) -> Kind<F, B>): Kind<F, List<B>>
```

list의 원소들을 하나씩 꺼내서 비동기 작업을 concurrent하게 실행하는... 그런 느낌이다. 이런 역할을 해주는 typeclass는 없는 것 같아서, 직접 custom하게 typeclass를 하나 정의했다.

```kotlin
interface ConcurrentMappable<F>: Monad<F> {
    fun <A, B> Kind<F, List<A>>.concurrentMap(f: (A) -> Kind<F, B>): Kind<F, List<B>>
}
```

그리고 Mono를 가지고 이를 구현하는 구현체를 작성해줄 수 있다.

```kotlin
class MonoConcurrentMappable(
    private val M: Monad<ForMonoK>
): ConcurrentMappable<ForMonoK>, Monad<ForMonoK> by M {
    override fun <A, B> Kind<ForMonoK, List<A>>.concurrentMap(f: (A) -> Kind<ForMonoK, B>): Kind<ForMonoK, List<B>> =
        this.fix().mono
            .flatMapMany {
                Flux.fromIterable(it)
            }
            .flatMap {
                f(it).fix().mono
            }
            .collectList()
            .k()
}
```

## Configuration: 마법이 일어나는 곳

지금까지 작성한 Service와 repository 코드들은 모두 Mono에 대해 알지 못하며, F에 대해 추상화된 코드이다. 한 마디로 빈 껍데기 뿐인 코드이며 이 코드들이 모여 하나의 어플리케이션으로 작동하기 위해서는 실제 의존성을 불어넣어주는 Configuration이 필요하다. 

```kotlin
@Configuration
class UserConfiguration {
    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    private val monoAsync = MonoK.async()

    @Bean
    fun userRepository(): UserRepository<ForMonoK> =
        UserRepositoryImpl(monoAsync, userJpaRepository)

    @Bean
    fun concurrentMappable(): ConcurrentMappable<ForMonoK> =
        MonoConcurrentMappable(monoAsync)

    @Bean
    fun userService(): UserService<ForMonoK> =
        UserServiceImpl(monoAsync, concurrentMappable(), userRepository())

    @Bean
    fun userHandler(): UserHandler =
        UserHandler(userService())

    @Bean
    fun userRoutes(): RouterFunction<*> =
        UserRouter(userHandler()).userRoutes()
}
```

Configuration이 적용되고 나서야 우리가 작성한 코드들은 runtime에 Mono 의존성이 주입되어 우리가 원하는대로 동작하게 된다.

만약 `Mono`를 `Observable`이나 `Future`로 바꾸고 싶다면 우리가 해야 할 것은 이 Configuration 파일 하나를 고치는게 끝이다. 아주 멋진 일이다!