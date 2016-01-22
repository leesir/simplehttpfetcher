##simplehttpfetcher是什么?

simplehttpfetcher是一个基于HttpClient4的http请求工具，用于简化项目中的http访问。

##simplehttpfetcher有哪些功能？

simplehttpfetcher旨在解决http请求数量过多而造成应用执行慢的问题。内部提供2中访问http的方式：

* 单线程访问方式
* 基于fork-join框架的多线程访问方式

经测试，fork-join方式的http请求方式，能有效降低50次以上的http访问的总时间（相比于单线程方式的访问）。

##simplehttpfetcher有哪些缺点？

simplehttpfetcher获取http结果的方式是阻塞的，仅当所有http的访问都已经返回之后，才能进行下一步操作。对于http结果内容庞大的应用来说，会有内存溢出等问题。

因此simplehttpfetcher特别适合于一次性访问http接口次数特别多并且每次访问返回结果都不大的应用场景。

##有问题反馈
如果有任何意见和建议，请联系我：

* 邮件(jlee381344197#gmail.com, 把#换成@)
* QQ: 381344197
* github博客：http://leesir.github.io/
