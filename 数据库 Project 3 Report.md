# 数据库 Project 3 Report

## 写在前面

很多时候，我们遇到一个已经写了一大半的 Project, 便下意识地感到困难棘手，这并不是幻觉。阅读一份已经完成了一大半的代码，并不比从头开始编写代码简单多少，尤其是在我们这种情况下：这份代码并不是残缺的，而是未完成的。

残缺的代码，和未完成的代码，有着本质区别：

- 残缺的代码指的是一个完整的 Project 去掉部分结构让我们如填词般将我们认为正确的、妥当的内容加进去。
- 未完成的代码则是一个不完整的 Project 交给我们去将其实现为一个完整的 Project. 

但是，如果本身这段代码就是不成功的，或者说结构不合理的，我们对它进行完善，本身就是舍近求远的事情。

但那又有什么办法呢，我们并无更多的选择，Let's just begin.

## 数据库结构描述

首先，在开始编写代码之前，让我们先留心分析一下数据库当前的结构及其相关的依赖关系。

数据库中的表格如下：

- **User**: 描述“人”作为实体的表格

- Instructor: 存储教师相关信息的表格
  对 User 进行依赖。

- **Department**: 描述“系”的表格

- Major: 描述专业的表格
  所有专业都有管辖它的系负责。

- Student: 描述学生的表格
  既有对 User 的依赖——学生从中获得它的 ID, 
  也有对 Major 的依赖——学生隶属于某个专业。

- **Course**: 描述课程的表格
  课程并不是具体的直接进行知识传授的实体，而是对课段的抽象汇总，也是专业划分下的最小单位。

- Major_Course: 存储相关专业的专业课信息
  其中 property 属性存储相关信息：专业必修课、专业选修课(或者更多)

- **Semester**: 描述学期的表格
  学期有具体的开始和结束时间，学期名称，也有对应生成的学期 ID. 

- CourseSection: 描述课段的表格
  课段指的是可供学生选择课程的一种衡量单位，一个学生只要选择了某一个课段进行修习并完成获得成绩，它便得到了对应的课程学分。
  所有任何课段都有对应负责的课程(Course), 也有对应开设时间的学期(Semester). 

- prerequisite: 先修课描述
  以 ltree 的形式进行存储，比较复杂，直接调用 *passed_prerequisites_for_course(studentID integer, courseID varchar, NULL ltree, NULL integer)* 可以获得是否通过先修课的回答：TRUE: 表示已经通过先修课。

  笔者并没有对该 sql 函数进行阅读，时间仓促。只能够仓促得到结果并尝试检验，结果无误。

- CourseSectionClass: 具体周课时描述（之后统一简称“课时”）
  课时指的是对 course section 进行具体描述的内容，是学生进行课程的单位。值得注意的是，学生选课的最小单位是：course section, 但学生进行课程的单位是：course section class. 这两者有着严格的不同，请不要混淆。

- student_section: 学生选课成绩记录表
  记录学生的选课成绩。
  ~~这个表格居然是小写字母加下划线描述的，命名法使用极其混乱，这也正是我在开始所抱怨的：这并不是一个合理架构过的 project, 更像是一个东拼西凑出来的残次品，甚至还不如——它还未完成，某种角度上看，我看不到它能够完成的潜力。~~

上述表格中，我对不对其他表格有依赖关系，也就是拥有最基本的数据描述的表格的表格名进行了加粗表示，以让读者更容易感受到它们的重要性和必要性。

## 数据结构重整

讨论完表格的结构以后，我们接下来，便要增添对表格结构描述的约束规则：避免一切愚蠢错误的发生。

当然，在这一小节中，本人非常乐意使用更加丰富、多样的例子，以更加深入浅出地解释清楚我们的数据到底都以什么样的方式进行组织的。

### Department

Department 是存储院系的表格，里面每条数据都对应一个实体院系。

- departmentId: serial, NOT NULL, primary key.
- name: NOT NULL, unique(Index). 

观察约束，可以得知它的约束设置和现实相符：一个院系在创建的过程中会自动生成对应的 ID, 当然，对于数据库来说，也可以自动为它赋予对应的 ID, serial 的设置正是为此而来。

不仅如此，一个院系应当有一个明确的名称——这个名称不应该与其他院系相同，否则我们怎么称呼这个院系呢？一所学校不应当有两个 “物理系”，也不应当有一个“NULL”系。

至于 departmentId, 更像是一个添头——或者说，出于节约空间的目的，而使用的一个映射键，它本身并不承载真正物质实体上的含义，而是一个单纯的映射描述，或者说是一种“简写”，来指代我们真正描述的实体：院系名称。

请不要为这点感到困惑：虽然我个人也同意为一个虚无的键设置 primary key 是一件亏本的生意，但 project author 恐怕被没有那么深谋远虑，而是出于好看，或者说一些微不足道、细枝末节的理由，便匆匆做了这种计划，虽然不让人认可，但也不必大惊小怪便是了。

当然，这样也产生了两点好处：首先，简写带来空间的节约，如果我们之后的依赖项，“专业”有大量的依赖键引用我们的 department, 那么提前预处理便能够真真切切的节省我们的数据库空间；其次，它在削减数据库的耦合度的同时，也增加了它的复杂性，增加了查询的压力——想要通过 departmentId 去查询 name 成了一件不太划算的事情，一般情况下我们也不愿意这么做，说不定，这也是 ex project author 的深谋远虑吧。

相关代码如下：

```sql
create table "Department"
(
    "departmentId" serial  not null
        constraint department_pk
            primary key,
    name           varchar not null
);

alter table "Department"
    owner to postgres;

create unique index department_departmentid_uindex
    on "Department" ("departmentId");

create unique index department_name_uindex
    on "Department" (name);
```



### Course

Course 是一个描述课程的表格，值得注意的是，<u>笔者对课程的描述进行了三个层次的分层：课程、课段和课时。</u>

> Course:（课程）
>
> 课程是学生的培养方案中对学习描述的最小单位，所有专业的学生都应该根据培养方案，选择对应的课程进行学习并获得 Pass 或不低于 60 分的成绩，以修得该课程的对应学分。
>
> Course Section: （课段）
>
> 课段是学生在每个学期的学习中，选择课程的标准单位。任何一个课段都对某一门课程负责，表示这门课段教授对应课程的知识内容。但一门课程往往拥有不同的课段进行知识传授，它们可能分布于同一个学期，也可能是不同的学期。
>
> 任何一课段，都应该传授它所对应的课程的所有知识，尽管每一课段的授课者可能不同，并且都可能是多个，但它们所涵盖的知识点都应该相同，考核难度相近，以便于学生选择不同课段的学习过程，所得的该课程成绩不应该与课段本身有太大关系，以导致课程公平性问题。
>
> Course Section Class: （课时）
>
> 课时是学生进行具体课程的单位，也是对应课段的开设者进行授课的基本单位。理论上，任何一门课段的授课时间，都应该下放到课时中去描述，比方说：
>
> 数字逻辑课程的授课课时：
>
> 星期一 第 5-6 节 荔园 6 栋 403 （ 1-5 周） Professor ...
>
> 星期四 第 7-8 节 荔园 6 栋 402 机房 （1-5 周）Lab Teacher ...
>
> 可以看出，课时所对应的是学生在选择对应的课段后，具体进行课程的安排描述。

- courseId: NOT NULL, primary key. 
- courseName
- credit
- classHour
- grading

易知，该规则中，对于 Course 本身的 constraint 并不丰富，仅仅只对 courseId 进行了相关约束，但这也并不奇怪：我们的数据库 project 只在乎如何选课、退课、查询课程，并不在意太多其他事情，而 courseName 在这里其实是意义寥寥的，因为 courseId 本身便是一个内涵丰富的关键字——它并不同于 departmentId 那样，是一个虚无的描述，courseId 本身便蕴含了足够的课程描述，因此，courseName 在此仅仅成了可有可无的一项，这一点值得读者注意。

此外，credit, classHour 同样并不是关键的数据，我们在本次 project 中，其实并不在乎一个 Course 到底占据了多少 credit, 至少，我们并不需要给出一个学生已经获得了多少学分的论断，所以这一项便一不小心成了可有可无的——所有的运算都与它无关，不过也许我们在查询的时候，需要得到这个结果。

至于 classHour, 我起初以为它是重要的——它可以指点我们的 section 下的 class 是否合情合理，它们占据的对应时长是否与 course 相吻合——但从 project 的难度推断，这列无意义。

grading 反而是很重要的一项：它形成了关于课程描述的新约束，我们在加入学生的 section grade 的时候，不得不谨慎地判断学生是否正确得到了对应课程的成绩，如果在一门 PASS_OR_FAIL 的课程中，我们的学生居然得到了 100 的好成绩，我们便很难相信这件事情是真实的，无论是否相信，出于维护数据完整性的必要情况来看，更稳妥的做法显然是：忽略这条 insert sql statement, 而不能把它真正加入到我们的数据中，如果忘记对这部分内容进行处理，数据便很可能遭受“污染”，我们已经搞不清我们面对的数据是否是真实的了。

```sql
create table "Course"
(
    "courseId"   varchar not null
        constraint course_pk
            primary key,
    "courseName" varchar,
    credit       integer,
    "classHour"  integer,
    grading      varchar
);

alter table "Course"
    owner to postgres;
```

### User

User 是一个描述用户的表格，这里的用户概念并不清晰，乍听上去有点像是一个管理数据库的抽象层次，然而事实上并不如此，它们仅仅是非常平凡的层次，数据中的一部分，并且没有什么特殊的地方。

- id: NOT NULL, primary key
- fullName

本数据库中，id 无论是对于何种对象来说，恐怕都是核心、重中之重的属性，而在描述 User 的过程中，也同样如此。

值得注意的是，User 本身承载的信息并不重，不过 fullName 对全名的描述往往是在任何地方都被需要，这间接增强了 User 表格的重要性，否则其实以笔者本人的惰性，是不乐意召唤这样一个表格的——双列表格很难讨人喜欢，它往往被视为多余。

fullName 尽管没有任何约束，但我们只能在此祈祷我们的数据库在传入数据的过程中，能够足够友善——不要轻易影响了我们的表格运行，因此我不打算使用直接 `INSERT INTO User` 这样的 clause, 而是把这样的句子放在抽象层次更高的方法中，以稍稍保护我们的代码，避免不法侵害。

```sql
create table "User"
(
    id         integer not null
        constraint user_pk
            primary key,
    "fullName" varchar
);

alter table "User"
    owner to postgres;
```

### Instructor

Instructor 是一个描述教授信息的表格，从面向对象的逻辑考虑，它显然和我们上文所提到的 User 有着明显的继承关系，一个 instructor 也显然是一个 user. 

那么它们显然也使用了一脉相承的 userId 的方式，去描述这样一个人物实例的特征属性，或者说是唯一属性。

- userId: NOT NULL, primary key, references User.id
- firstName
- lastName

值得注意的是，这个表格存储了 instructor 的姓名信息，在此处并没有组合在一起，而是以一种分成两列的方式存储——如果我们有耐心实现在上文中描述的 full name parser, 那么这个表格的存在便形同鸡肋，毫无价值——当然，我们并没有这么做，这也是这个所谓的 instructor 表格存在的意义了。

另一方面，这个表格的特殊价值，还在于设计的 first name, last name 可以参与到我们之后设计的 student service interface 方法 search course 中，我们可以通过对某些 sections 中 classes 是否存在我们喜欢的 instructor 进行查询、搜索和筛选。

```sql
create table "Instructor"
(
    "userId"    integer not null
        constraint instructor_pk
            primary key
        constraint instructor_user_id_fk
            references "User"
            on delete cascade,
    "firstName" varchar,
    "lastName"  varchar
);

alter table "Instructor"
    owner to postgres;
```

### Major

Major 是一个描述学生专业的表格，每条条目的信息都存储了一个专业的相关信息，包括：专业 id, 专业名，及专业所归属的院系。

- id: serial, NOT NULL, primary key
- name
- departmentId: references Department.departmentId

其实我有点怀疑 `references table_name on delete cascade` 的用法，是否能够真正地形成对应地表格依赖——我对 project contributor 其实没有多少信任，或者本身他也没有可以信任的基础和价值。

另一方面，只建立 references 约束，而没有 NOT NULL 带来的恶果就是，我能够随意、肆意妄为地往里面填充 NULL 信息以生成大量的无意义行数据，如果这个漏洞在我们的 Project Judge 被滥用，那无疑出题者是缺德的。

此外，name 在本表格中也没有被增加相关约束：我们没能对专业的描述进行约束，非常可能出现同名的专业，这和我们之前所看到的 department 约束有相当的差异，不过事情还不算太糟是因为：我们的 project 本身考点并不在此，所以我们大可不必在意出现大量相同 major name 的情形，这种困难理论上并不会在我们的方法实现中发生，也不至于对我们造成困扰，所以我不妨先把这一点点不完美放开，继续专心思考我们遭遇的困惑和问题。

```sql
create table "Major"
(
    id             serial not null
        constraint major_pk
            primary key,
    name           varchar,
    "departmentId" integer
        constraint major_department_departmentid_fk
            references "Department"
            on delete cascade
);

alter table "Major"
    owner to postgres;
```

### Student

Student 是一个存储学生相关信息的表格，其中它也描述了 student 和 user, major 的相关依赖关系，可以说是本次 project 中的数据库结构中的一大核心结构，与各个表格都有或多或少的联系。

- enrolledDate
- majorId: references Major.id
- userId: NOT NULL, primary key, references User.id
- firstName
- lastName

其中，enrolledDate 和之前我们所谈到的 credit, classHour 非常相似，是一个没什么意义的数据，我们不妨先放在一旁，不必理会，让这种虽然隶属于我们的 Student 但实则毫无约束、游离在外的数据自个玩去。

majorId 描述了该学生的专业，由于没有做 NOT NULL 约束，我们也许会遇到没有专业的学生——这和实际情况相符，但由于我们并不在意这个学生的专业情况，用更准确的术语说：我们的 project 并不在乎学生的专业，所以这一列不妨也先放在一旁吧。

userId 标识了用以区分 student 的属性，同时也描述了对 User 表格的依赖，这意味着：我们如果想要直接、了当地添加我们的学生，必须自然地将我们的学生同时（甚至提前）添加到 Table User 中。

firstName, lastName 属性则同 instructor 一样，意义寥寥。

```sql
create table "Student"
(
    "enrolledDate" date,
    "majorId"      integer
        constraint student_major_id_fk
            references "Major"
            on delete cascade,
    "userId"       integer not null
        constraint student_pk
            primary key
        constraint student_user_id_fk
            references "User"
            on delete cascade,
    "firstName"    varchar,
    "lastName"     varchar
);

alter table "Student"
    owner to postgres;
```

### Semester

Semester 存储了学期的相关信息，包括学期名，该学期的开始、结束时间，和它的独特 ID. 

- id: serial, NOT NULL, primary key
- name
- begin
- end

其实这个表格虽然简单，但让我担心的事情其实很多：begin, end 列并没有被设置 NOT NULL constraint, 这意味着我们可能遇到一些学期没有开始时间、结束时间，遇到这种事情的后果往往是灾难性的。

我不得不在此提醒，如果数据库不慎存储了这种类型的数据——那我们在提取相关 semester 的时候，原则上必须要忽略掉这类“异样”的数据，只提取出有 begin, end 的 semesters, 像一个净水器那样，筛掉那些污染物。

```sql
create table "Semester"
(
    id    serial not null
        constraint semester_pk
            primary key,
    name  varchar,
    begin date,
    "end" date
);

alter table "Semester"
    owner to postgres;
```

### CourseSection

CourseSection 表示课段信息，每条信息表示一个有效课段，课段是隶属于课程的一个子项目，是抽象课程的具体描述和客观体现，也是学生直接接触和选择的实体。

- sectionId: serial, NOT NULL, primary key
- totalCapacity
- leftCapacity
- courseId: references Course.courseId
- semesterId: references Semester.id
- sectionName

课段信息描述中，sectionId 作为其中的特征信息，使用 serial, primary key 进行描述，是每一课段的唯一标识符，学生对课段的选择也通过 sectionId 建立相关的联系关系。

totalCapacity 描述该课段的总容量，即选择这门课程的学生的最大值。

leftCapacity 描述该课段的剩余容量，即这门课程还可供多少学生选择。当有学生通过 enroll course 进行课程选择的时候，在选择成功后，便会造成 leftCapacity 发生变化，使得 leftCapacity = leftCapacity - 1. 

当然，既然存在选课，自然也存在对应的逆向操作：退课 drop course. 在逻辑正确的情况下，顺利地执行了退课操作，我们应当适当地设置成：leftCapacity = leftCapacity + 1. 

虽然这听起来非常得美妙，但我们的计划显然不会那么顺利：project 预留了一个特殊的强制选课操作：enroll course with grade. 这个看上去像导入数据的操作中，却预留了特殊的加课功能——这种类型的加课是不会改变 leftCapacity 的，这也就意味着，我们在 drop 这种非法加入的课程的时候，也不应该改变 leftCapacity 的值，否则可能会出现 leftCapacity > totalCapacity 的谬论。

这不是件好办的事。

而 courseId, semesterId 如出一辙，这可以说是这个 project 的传统艺能了：不设 NOT NULL, references 不明确——其实悄悄说，我认为这种 references 写法不仅仅是不规范的，它甚至可能是错误的。

**它的错误性我会在之后进行实验证明。**

但我只能非常无奈地把 :warning:Warnings 留在这里：谨慎遇到无依赖关系的数据选取。

至于 sectionName, 这恐怕又是一个出于美观、好看而设立的属性罢了，意义全无，便不管它即可。

```sql
create table "CourseSection"
(
    "sectionId"     serial not null
        constraint coursesection_pk
            primary key,
    "totalCapacity" integer,
    "leftCapacity"  integer,
    "courseId"      varchar
        constraint coursesection_course_courseid_fk
            references "Course"
            on delete cascade,
    "semesterId"    integer
        constraint coursesection_semester_id_fk
            references "Semester"
            on delete cascade,
    "sectionName"   varchar
);

alter table "CourseSection"
    owner to postgres;
```

### CourseSectionClass

Course Section Class 是存储课时的表格，这是在 Course Section Layout 的下属层级。

- id: serial, NOT NULL, primary key
- instructor: references Instructor.userId
- dayOfWeek
- location
- weekList
- sectionId: references CourseSection.sectionId
- classStart
- classEnd

Class, 也就是我所描述的课时，也就是我们现在所使用的表格的主角，每条数据都在尝试描述一个具体的 class. 

显然，此 class 并非我们所以为的那样，表示一个班——不过这也说不准，它可以体现一个班的某种特质，但不应该轻易被划分成一个班，毕竟，一个 section 对应了多个 class, 假使甲乙丙丁都选择了 section A, 对应了 class a, b, c. 那我们要说甲乙丙丁都在 class a 中呢，还是在 class b 中呢？

甚至更倒霉的事情是，有赵钱孙选择了 section B, 对应了 class b, d. 那现在我们不得不面对一个惨淡的现实：赵钱孙和甲乙丙丁是否在一个班中呢？

所以，我们还是草草解释成课时便可，不必过多对其进行解释。

instructor 显然便是我们之前所讨论的，该课时的所有者、授课者——这种数据组织方式隐约透露给我们两件事情：

1. 具体的某个课时可能没有显示地描述授课者，即授课者列可能是 NULL. 
2. 对于同一个 course section, 不同的课时 class 往往有着不同的授课者，当然，这也是一件非常普遍的事情。

dayOfWeek 试图给我们描述清楚该课时对应的是一个星期中的具体某天，不过如果有别有用心的人把它变成了 NULL, 或者写个 "Monkeyday" 之类的字符串，我们显然会感到迷茫，"Monkeyday" 是哪一天呢？如果有人放了一个 "National Day" 我们该怎么办？

我的回答是：不要理他。所谓：见怪不怪，其怪自败，正是如此。

location 也好，weekList 也好，里面存储的信息——其实我们也说不准都有哪些意外发生，没有约束真是一种勇敢的选择，不过不必在意，我们目前大可不必考虑这么多，这些东西在很久以后才需要被考虑和处理，就让我们忘了这一切不愉快吧。

classStart and classEnd 描述了该课时当天的起止时间，事实上这部分信息在我们进行 search course 的时候，有必要纳入考虑之中，不过现在我们就不用考虑太多，更多的想法之后再说吧。

```sql
create table "CourseSectionClass"
(
    id           serial not null
        constraint coursesectionclass_pk
            primary key,
    instructor   integer
        constraint coursesectionclass_instructor_userid_fk
            references "Instructor"
            on delete cascade,
    "dayOfWeek"  varchar,
    location     varchar,
    "weekList"   smallint[],
    "sectionId"  integer
        constraint coursesectionclass_coursesection_sectionid_fk_2
            references "CourseSection"
            on delete cascade,
    "classStart" smallint,
    "classEnd"   smallint
);

alter table "CourseSectionClass"
    owner to postgres;
```

### Major_Course

> 如果 project 的初代作者还有点脑子的话，就应该想清楚坚持什么类型的命名法。
> -- Cutie Deng

其实吧，使用这种低劣的下划线命名法，我还是能够稍稍理解的——这个类和一般的类不一样，它描述的是两个客观实体之间的联系，而不是对某个实体进行阐述和解释，所以将两种实体名以并列的方式相提并论，某种意义上说，还是有可取之道的，我并非没有原谅 original contributor 的理由，但读者很快在之后就没有办法再因为这些愚蠢、难以自圆其说的理由去原谅他了。

该表格尝试描述 Major 和 Course 之间的联系，比方说，“数据库原理”是“计算机科学与技术”的专业必修课，“C/C++程序设计”是它的专业选修课。

不过该表格似乎描述就到此为止，并没有更多、更加特别的东西呈现。

- majorId: references Major.id
- courseId: references Course.courseId
- property
- id: serial, NOT NULL, primary key
- *UNIQUE INDEX(majorId, courseId)*

读者们在阅读的时候请保持高度警惕，我再次强调：references 并不意味着 NOT NULL. 因为这种想法如此的顺其自然，以致于它往往有着易于销售的市场，但事实往往并不如人们心意，所以这种写法留下了大量的隐患：我们很难相信，一门课程成为了一个 NULL 专业的必修课。

~~同样，没有 UNIQUE 约束的结果是：我们很难相信，一门课程同时成为一个专业的必修课和选修课会怎样！~~

与此相比，property 本身的没有约束反而显得无足轻重了，反正 NULL 的 property 谁又会在乎呢？不过是一条没有信息量的数据罢了——随手删了都无所谓。

幸运的是，project 作者终于是意识到了他的错误，设置了这样一条 unique index 语句，差强人意。

```sql
create table "Major_Course"
(
    "majorId"  integer
        constraint major_course_major_id_fk
            references "Major"
            on delete cascade,
    "courseId" varchar
        constraint major_course_course_courseid_fk
            references "Course"
            on delete cascade,
    property   varchar,
    id         serial not null
        constraint major_course_pk
            primary key
);

alter table "Major_Course"
    owner to postgres;

create unique index major_course_majorid_uindex
    on "Major_Course" ("majorId", "courseId");
```

### student_section

如果说，刚刚的命名“独特”规范，是因为我们尝试创建了一种关系表，与之前的实体描述表有着强烈的不一致，那违规的小写描述又该作何解释呢？

不过，我们在这场 project 中，更愿意做一个旁观评议员，在没有绝对必要“出手”的时刻，我并不打算干预这有些混乱的结构，以防这愚蠢的结构产生意外——既然我们已经既定它如此 ugly, 那我们很难再相信它有良好的耦合度和内聚性，也许这些非预期的小修小补，会造成更多的破坏和劣质的数据描述出现。

步入正题。

student_section 描述学生对课程（课段）的选择情况描述，每条信息都表示一个学生对一个课段的有效选择，该条目的增添、删除应当被谨慎处理——谁能接受自己的成绩一下子被 delete 掉呢？

不过，出于我们对之前所抱怨的 enroll course with grade 方法使用的抱怨，我们不妨给一般的 enroll course 加点料——给它增添一个 ENROLL 描述。

- studentId: references Student.userId
- sectionId: refereences CourseSection.sectionId
- grade

除去我们频频抱怨的 studentId, sectionId, 我们在此不妨额外注意另一件事情：grade 的值情况。

我们将会通过两个特殊的方法去执行对该表格的修改：

1. enroll course: 学生选课，在该过程中需要经历一系列艰辛的判断，最后我们宽大为怀地“认证”，该学生可以选择这门课程，于是为他添加上相关的信息。
2. enroll course with grade: 强制选课，在该过程中，我们既不分析学生是否满足课程的前置条件，也不在乎学生的课表是否支持这种情形的发生，反正，什么都不在乎，就把这条数据给它加上去！

第二种情况如果被滥用，导致的结果必然是污染性的。

如果遇到，就算我们倒霉吧，就这样吧。

```sql
create table student_section
(
    "studentId" integer
        constraint student_section_student_userid_fk
            references "Student"
            on delete cascade,
    "sectionId" integer
        constraint student_section_coursesection_sectionid_fk
            references "CourseSection"
            on delete cascade,
    grade       varchar
);

alter table student_section
    owner to postgres;
    
create index name1 on student_section("studentId");
create index name2 on student_section("sectionId");
```

## 方法创建分析

### Department Management

#### add_Department

该方法接受单个参数：nameIn: 表示该院系的名称。

调用该方法将创建一个新的院系，将可以用来下设对应的专业描述。

返回值描述：integer, 表示该院系的 ID. 

:warning: 如果企图加入一个已经存在的同名院系，会违反 unique index 约束，导致 exception:

错误： 重复键违反唯一约束"department_name_uindex"

不过，希望读者不要对 index 产生误解——index 并不能起到像 primary key 那样，禁止 update 的作用，但愿我们的数据不会被突如其来的 update 毁灭。 

```sql
create or replace function add_Department(nameIn varchar) returns integer
    language plpgsql
as
$$
BEGIN
    insert into "Department" (name) values (nameIn);
    RETURN (select "departmentId" from "Department" where name = nameIn);
END
$$;
```

#### remove_Department

该方法用于移除对应的院系，类似于加入院系的逆操作。

原则上，它并不能够起到消除 add Department 影响的作用，因为它的操作对 serial 的影响是永久性的。

本人印象中有一些办法可以尝试对 serial 进行刷新，不过 2^31^ 的范围对于我们的“小打小闹”来说，无论如何来说，都是极度充裕的。所以，先把这个问题搁置在一边吧。

该方法接受一个参数：departmentId integer. 表示将要移除的 department 的 ID. 

:warning: 如果在数据库中找不到相应的 ID 值，该方法将会抛出相应异常：

错误：Cannot find department. 

*该异常的具体描述并不是原 project 中设置的，笔者在原代码的基础上进行了部分补充。*

该方法没有返回值。

```sql
CREATE OR REPLACE FUNCTION remove_Department(departmentId int)
    returns void
as
$$
BEGIN
    IF ((select count(*) from "Department" where "departmentId" = departmentId) = 0) THEN
        RAISE EXCEPTION 'Cannot find department.';
    end if;
    DELETE
    FROM "Department"
    WHERE "departmentId" = departmentId;
END
$$
    LANGUAGE plpgsql;
```

#### getAllDepartments

返回整个 Department 表格，其中相关列名如下：

- departmentId_out
- name_out

> 看吧，狐狸尾巴露出来了，我都说了这厮压根不遵从任何命名法规矩，简直写得一塌糊涂。
>
> --Cutie Deng

我非常乐意为这个方法添加一个注解：@Deprecated. 不要在任何一段自己的代码中调用获取整个完整表格的方法，相信我，这绝对不是一件聪明的事情。

*某人已经用自己的血的教训证明了这件事情。*

```sql
CREATE OR REPLACE function getAllDepartments()
    RETURNS TABLE
            (
                departmentId_out integer,
                name_out         varchar
            )
AS
$$
BEGIN
    RETURN QUERY
        select "departmentId" as departmentId_out, "Department".name as name_out from "Department";
END
$$ LANGUAGE 'plpgsql';
```

#### getDepartment

在已知 department ID 的前提下，该方法能够获得相关的所有 departments 的相关信息——不过，众所周知，department ID 是一个 primary key, 所以一个 ID 至多对应一个 department 实体，所以这个方法的设置显得无比鸡肋。

那么，我们不妨也给它添加一句美妙的描述：@Meaningless

我似乎发现，这个 project 的初代作者并不只一个人，至少——写这个方法的人，比起写之前的方法的人来说，逊色许多。

一个不必使用的方法，就不介绍它的具体内容了，见代码吧。

```sql
CREATE OR REPLACE function getDepartment(department int)
    RETURNS TABLE
            (
                departmentId_out integer,
                name_out         varchar
            )
AS
$$
BEGIN
    IF ((select count(*) from "Department" where "departmentId" = department) = 0) THEN
        RAISE EXCEPTION '';
    END IF;
    RETURN QUERY
        select "departmentId", "Department".name from "Department" where "departmentId" = department;
END
$$ LANGUAGE 'plpgsql';
```

### Major Management

#### add_Major

该方法用于向数据库中加入专业。

该方法有两个传入参数：

- nameIn varchar, 专业名称
- departmentId integer, 隶属院系 ID. 

某种意义上说，这个方法悄悄地屏蔽了 majorId 创建、赋值的过程，我算是有点明白为什么一定要把它们写成方法了——屏蔽部分不应暴露给 user 的接口。

隶属院系的 ID 设为 NULL 不影响该方法的顺利执行，不会被约束拒绝。

Major.name 是一个无约束量，我们有可能，在危险的情况下，会获得几个同名的专业，或者没有名称的专业。

方法返回 integer, 表示新增的 Major 的 ID. 

:warning: 如果加入了相同的 name, departmentId, 那么我们将无法返回正确的 ID, 取而代之的是：

错误：作为一个表达式使用的子查询返回了多列

在这种情况下，相同的 name, departmentId 并不会被真正加入表格中，某种意义上说，该方法也实现了 unique tuple 功能。

> 用这种方式来实现 unique, 倒是一种取巧得惊人的办法。
>
> -- Cutie Deng

```sql
CREATE OR REPLACE function add_Major(nameIn varchar, departmentId int)
    RETURNS integer
AS
$$
BEGIN
    insert into "Major" (name, "departmentId") VALUES (nameIn, departmentId);
    RETURN (select id from "Major" where name = nameIn and "departmentId" = departmentId);
END
$$ LANGUAGE 'plpgsql';
```

#### remove_Major

该方法负责移除相应的 Major. 

传入参数：majorId integer, 表示将要移除的 Major 的 ID. 

如果无法找到对应的 ID, 则 throw exception: 

错误：Cannot find major. （信息为笔者追加）

```sql
CREATE OR REPLACE FUNCTION remove_Major(majorId int)
    RETURNS VOID
    language plpgsql
AS
$$
BEGIN
    if ((select count(*) from "Major" where id = majorId) = 0) then
        raise exception 'Cannot find major.' ;
    end if;

    DELETE
    FROM "Major"
    WHERE id = majorId;
end
$$;
```

#### get_all_Majors

该方法将会返回所有专业形成的表格。

返回表格列参数：

- id: 专业对应的唯一 ID. 
- Major_name: 专业名称。
- department_Id: 管辖、负责该专业的院系 ID. 
- Department_Name: 院系名称。

该方法将会筛选掉没有受到院系管辖，即 departmentId = NULL 的专业的所有信息，形成一道特殊的滤网，只保留下对应、合适的信息，妙不可言。

能写出这种方法来，恐怕也不是投机取巧之辈，还算有点水平。

```sql
CREATE OR REPLACE function get_all_Majors()
    RETURNS TABLE
            (
                id              integer,
                major_name      varchar,
                department_id   integer,
                department_name varchar
            )
AS
$$
BEGIN
    RETURN QUERY
        select "Major".id,
               "Major".name           as Major_name,
               "Major"."departmentId" as department_Id,
               "Department".name      as Department_Name
        from "Major"
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId";
END
$$ LANGUAGE 'plpgsql';
```

#### get_Major

通过传入参数 majorId 获得对应的 major 信息表格。

这里要特别注意的是，在方法描述中，它的返回值不是一般我们所看到的 one row, 而是一个有点复杂的 TABLE. 我把它称之为：莫名其妙的故弄玄虚。

所以这方法虽然挺好理解，但也没啥意思，就随便看看吧。

```sql
CREATE OR REPLACE function get_Major(majorId int)
    RETURNS table
            (
                id              integer,
                major_name      varchar,
                department_id   integer,
                department_name varchar
            )
AS
$$
BEGIN
    if ((select count(*) from "Major" where id = majorId) = 0) then
        RAISE EXCEPTION 'Cannot find major';
    end if;
    RETURN QUERY
        select "Major".id,
               "Major".name           as Major_name,
               "Major"."departmentId" as department_Id,
               "Department".name      as Department_Name
        from "Major"
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId"
        where "Major".id = majorId;
END
$$ LANGUAGE 'plpgsql';
```

> 我突然发现写这些内容的作者大小写不区分，虽然说我们的 sql 语法在默认情况下也不区分大小写，但在自己进行代码的过程中，如果随波逐流地利用这种不好的特性来进行编程，代码的风格恐怕就指向了这位作者的上限。
>
> -- Cutie Deng

### Major & Course

#### add_Major_Compulsory_Course

为专业添加一门必修课程。

这可不是一个令人愉快的方法——谁愿意空降一门必修课程呢？

传入参数：

- majorId integer, 表示将要添加一门新的必修课的专业 ID. 
- courseId varchar, 必修课 ID

该方法并没有什么 NULL 检查，我们大可以使用空值“放心”地调戏她。

没有问题。

```sql
CREATE OR REPLACE function add_Major_Compulsory_Course(majorId int, courseId varchar) --Compulsory
    returns void
    LANGUAGE SQL
as
$$
insert into "Major_Course" ("majorId", "courseId", property)
VALUES (majorId, courseId, 'MAJOR_COMPULSORY');
$$;
```

#### add_Major_Elective_Course

同上，没有区别。

```sql
CREATE OR REPLACE FUNCTION add_Major_Elective_Course(majorId int, courseId varchar) --Elective
    returns void
    LANGUAGE SQL
as
$$
insert into "Major_Course" ("majorId", "courseId", property)
VALUES (majorId, courseId, 'MAJOR_ELECTIVE');
$$;
```

### Semester Management

#### remove_Semester

真是奇怪，这个方法的顺序居然是反的——连 semester 都没有添加呢，就猴急移除它了，学生党这么讨厌学期么？

> 这是个矛盾的学长
>
> -- Cutie Deng

和其他的移除方法没有显著区别，便不赘述了。

```sql
CREATE OR REPLACE FUNCTION remove_Semester(semesterId int)
    returns void
    LANGUAGE plpgsql
as
$$
begin
    if ((select count(*) from "Semester" where id = semesterId) = 0) then
        raise exception 'Cannot find semester';
    end if;

    DELETE
    FROM "Semester"
    WHERE id = semesterId;
end
$$;
```

#### add_Semester

添加一个新学期。

- nameIn varchar, 描述新学期的名称。
- beginIn date, 新学期的开始时间。
- end_In date, 新学期的结束时间。

:warning: 该方法并没有对新学期的合法性进行检查——既不能保证被加入的学期有开始和结束日期，也不能保证它的开始日期比结束日期要早。

返回新学期被加入后设置的 ID 值。

```sql
CREATE OR REPLACE function add_Semester(nameIn varchar, beginIn date, end_In date)
    RETURNS integer
AS
$$
BEGIN
    insert into "Semester" (name, begin, "end") VALUES (nameIn, beginIn, end_In);
    RETURN (select id from "Semester" where name = nameIn and begin = beginIn and "end" = end_In);
END
$$ LANGUAGE 'plpgsql';
```

#### get_all_Semester

> 这该死的命名法，它居然企图让我适应它。
>
> -- Cutie Deng

气死我了，不想解释这些愚蠢的内容。

获取整个学期表格。

:warning: 笔者对该方法进行了小小的修改——对 semester 的返回值进行了简单的过滤，排除了起止时间中有 NULL 发生的情况，以避免在学期日期计算的相关问题中发生错误。

```sql
CREATE OR REPLACE function get_all_Semester()
    RETURNS TABLE
            (
                id_out        integer,
                semester_name varchar,
                begin_        date,
                end_          date
            )
AS
$$
BEGIN
    RETURN QUERY
        select "Semester".id as id_out, name as semester_name, begin as begin_, "end" as end_
        from "Semester"
        where begin is not null and "end" is not null;
END
$$ LANGUAGE 'plpgsql';
```

#### get_Semester

根据对应的 semester ID, 获取对应的 semester 所有信息。

这个方法写得太麻烦了，笔者懒得改，反正，没有哪个傻瓜会用这个方法去实现自己的事情吧？

代码已经足够清晰了，不做解释：

```sql
CREATE OR REPLACE function get_Semester(semesterId int)
    RETURNS TABLE
            (
                id_out        integer,
                semester_name varchar,
                begin_        date,
                end_          date
            )
AS
$$
BEGIN
    if ((select count(*) from "Semester" where "Semester".id = semesterId) = 0) then
        raise exception 'Cannot find semester. ';
    end if;
    RETURN QUERY
        select "Semester".id, name, begin, "end"
        from "Semester"
        where "Semester".id = semesterId;
END
$$ LANGUAGE 'plpgsql';
```

### User Management

#### remove_User

根据 User ID, 移除对应的用户。

这个方法看起来简单，但突然提醒了我一件事情：delete 表格信息是连锁的。

也就是说，我们通过该方法删除对应的 User ID, 同时也会连锁删除对应的 Student 或 Instructor 中的对应条目的所有信息，理论上。

那这 remove 方法，写得还算可圈可点，虽然没啥可以夸的地方，但也没啥可以骂的地方。

```sql
CREATE OR REPLACE FUNCTION remove_User(userId int)
    RETURNS VOID
    language plpgsql
AS
$$
BEGIN
    if ((select count(*) from "User" where id = userId) = 0) then
        raise exception 'Cannot find user. ' ;
    end if;

    DELETE
    FROM "User"
    WHERE id = userId;
end
$$;
```

#### get_all_users

获取所有的 user. 

这个方法可不一般——不要误以为它只是获取了对应的 User 表格中的所有信息，它所做的远远不仅仅如此：它还获取了学生的 enrolledDate, department_name, major_name 等等相关信息。

但它也许也没有我们想象地厉害——它在连接 Instructor 的时候好像做了一件毫无意义的事情，但这又有谁在意呢？

只不过，这个方法谈得上有趣，谈不上有多么优秀罢了。

```sql
create or replace function get_all_users()
    returns TABLE
            (
                userId          integer,
                fullName        varchar,
                enrolledDate    date,
                department_name varchar,
                MajorId         integer,
                major_name      varchar,
                department_id   integer
            )
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        select "User".id, "fullName", "enrolledDate", department_name, "majorId", major_name, "Major"."departmentId"
        from "User"
                 left join "Student" on "User".id = "Student"."userId"
                 left join "Instructor" on "User".id = "Instructor"."userId"
                 left join "Major" on "Student"."majorId" = "Major".id
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId";
END
$$;
```

#### get_User

根据对应的 user ID, 获取相应的用户信息。

过程和 get_all_users 的思路基本一致，没啥创新点和被优化的地方。这种水平，就不要瞎写东西来糊弄人了，我要是只看这个代码块，乍一看可被吓唬到了——各式各样的命名结构、牛鬼蛇神都出现了，也不知道是从哪里跑出来的大杂烩。

:eye_speech_bubble: 万一被误认为是从互联网上东拼西凑出来的四不像就不好了，对不对。

@Deprecated

```sql
create or replace function get_User(userIdIn integer)
    returns TABLE
            (
                userId          integer,
                fullName        varchar,
                enrolledDate    date,
                department_name varchar,
                MajorId         integer,
                major_name      varchar,
                department_id   integer
            )
    language plpgsql
as
$$
BEGIN
    if ((select count(*) from "User" where id = userIdIn) = 0) then
        raise exception '';
    end if;
    RETURN QUERY
        select "User".id, "fullName", "enrolledDate", "Department".name, "majorId", "Major".name, "Major"."departmentId"
        from "User"
                 left join "Student" on "User".id = "Student"."userId"
                 left join "Instructor" on "User".id = "Instructor"."userId"
                 left join "Major" on "Student"."majorId" = "Major".id
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId"
        where "User".id = userIdIn;
END
$$;
```

### Instructor Management

#### add_Instructor

添加一个教师。

传入参数：

- userId integer, 用户 ID.
- firstName varchar, 用户姓名的前一部分。
- lastName varchar, 用户姓名的后部分。
- fullname_in varchar, 用户全名，由于 project 作者水平低下，只能够先行在 java 中计算出结果，再传入数据库中进行存储——所以该数据虽然从 first name, last name 中衍生出来，但依旧拥有相同的数据优先级。

传出参数： 用户 ID. 

其实这个传出参数没啥意义，可能是为了逗大家开心才加上的。

```sql
CREATE OR REPLACE function add_Instructor(userId int, firstName varchar, lastName varchar, fullname_in varchar)
    RETURNS integer
AS
$$
BEGIN
            insert into "User" (id, "fullName") VALUES (userId, fullname_in);
    insert into "Instructor" ("userId", "firstName", "lastName") VALUES (userId, firstName, lastName);
    RETURN userId;
END
$$ LANGUAGE 'plpgsql';
```

#### get_Instructed_CourseSections

大发现啊，原来我们已经拥有方法根据 instructor 获取 section 了，亏我之前写了半天。

> 书读百遍，其意自见。
>
> -- Cutie Deng

传入参数：

- instructorIdIn integer, 教师 ID. 当该 ID 不存在时，错误：Cannot find instructor. 
- semesterIdIn integer, 学期约束，查询的 section 隶属的学期的 ID. 学期 ID 不存在时，错误：Cannot find semester. 

传出参数：

- courseSectionIdOut integer, 满足筛选要求的课段 ID.
- courseSectionNameOut varchar, 对应的课段名称。
- leftCapacityOut integer, 课段剩余容量。
- totalCapacityOut integer, 课段总容量。

:warning: 该方法可能会获得并不真正被 course 管辖的特殊 section 值，也就是污染，使用时如果出错可以从该方面进行考虑。

```sql
CREATE OR REPLACE function get_Instructed_CourseSections(instructorIdIn int, semesterIdIn int)
    RETURNS TABLE
            (
                courseSectionIdOut   int,
                courseSectionNameOut varchar,
                leftCapacityOut      int,
                totalCapacityOut     int
            )
AS
$$
BEGIN

    if ((select count(*) from "Instructor" where "userId" = instructorIdIn) = 0) then raise exception 'Cannot find instructor. '; end if;
    if ((select count(*) from "Semester" where id = semesterIdIn) = 0) then raise exception 'Cannot find semester. '; end if;

    RETURN QUERY
        select "CourseSection"."sectionId", "sectionName", "leftCapacity", "totalCapacity"
        from "CourseSectionClass"
                 left join "CourseSection" on "CourseSectionClass"."sectionId" = "CourseSection"."sectionId"
        where instructor = instructorIdIn
          and "semesterId" = semesterIdIn;
END
$$ LANGUAGE 'plpgsql';
```

### Course Management

#### add_Course

添加一门课程。

传入参数：

- courseId varchar, 课程 ID. 
- courseName varchar, 课程名称。
- credit integer, 修习该门课程能够获得的学分。
- classHour integer, 该门课程修习的建议学时。
- grading varchar, 该课程的计分方式，理应只有两种值：PASS_OR_FAIL 或者 HUNDRED_MARK_SCORE.

但该方法并没有对这些传入参数进行限制，而又由于本来 Course 是一个非常独立的实体描述，所以我们很难确定都有哪些参数被传入其中——我只能保证，我们只选择有效、合法的数据来进行分析，至于那些污染物，我们的原则是：**视而不见**。

```sql
CREATE OR REPLACE function add_Course(courseId varchar, courseName varchar, credit int, classHour int, grading varchar)
    returns void
AS
$$
BEGIN
    insert into "Course" ("courseId", "courseName", credit, "classHour", grading)
    VALUES (courseId, courseName, credit, classHour, grading);
END;
$$ LANGUAGE 'plpgsql';
```

#### remove_Course

> 无法相信，一个连单词都拼错的人，可以胜任什么工作。
>
> -- Cutie Deng

有点立 Flag 的感觉，因为我在写这篇 report 的时候就“特地”关闭了 editor 的自动语法检查，即使我一不小心打错了某个字、某个词，我也可能觉察不了，不过，这就当对自己的考验吧。

*如果发现了某个字词的错误，欢迎投诉。*

该方法用于移除已经被添加的课程，传入参数：courseId varchar, 表示应该被删除的 course ID, 其实这个条件有点苛刻了，不过勉为其难，不必在乎。

如果问笔者有什么建议的话，为什么不通过一个 bool 的返回值来描述清楚这件事情到底发生了没有呢？

把凡是依赖于 exception handle, 是一种水平很低的体现哦。

```sql
CREATE OR REPLACE function remove_Course(courseId varchar)
    returns void
AS
$$
BEGIN
    if ((select count(*) from "Course" where "courseId" = courseId) = 0) then raise exception 'Cannot find course. '; end if;

    delete from "Course" where "courseId" = courseId;
END
$$ LANGUAGE 'plpgsql';
```

#### remove_Course_Section

删除课段。

传入参数：sectionId integer, 要删除的课段 ID. 

结构简单，不赘叙。

```sql
CREATE OR REPLACE function remove_Course_Section(sectionId int)
    returns void
AS
$$
BEGIN
    if ((select count(*) from "CourseSection" where "sectionId" = sectionId) = 0) then raise exception 'Cannot find course section. '; end if;

    delete from "CourseSection" where "sectionId" = sectionId;
END
$$ LANGUAGE 'plpgsql';
```

#### remove_Course_Section_Class

```sql
CREATE OR REPLACE function remove_Course_Section_Class(classId int)
    returns void
AS
$$
BEGIN
    if ((select count(*) from "CourseSectionClass" where id = classId) = 0) then raise exception 'Cannot find class. '; end if;

    delete from "CourseSectionClass" where id = classId;
END
$$ LANGUAGE 'plpgsql';
```

#### get_Course_Sections_In_Semester

根据 semester 和 courseId 获得所有相应的 course sections. 

没啥特别的，和[get_Instructored_CourseSections](#get_Instructed_CourseSections)没啥区别，方法效率勉勉强强，就图个乐子吧。

```sql
CREATE OR REPLACE function get_Course_Sections_In_Semester(courseId varchar, semesterId int)
    RETURNS TABLE
            (
                idOut         integer,
                nameOut       varchar,
                leftCapacity  integer,
                totalCapacity integer
            )
AS
$$
BEGIN

    if ((select count(*) from "Course" where "courseId" = courseId) = 0) then raise exception 'Cannot find course. '; end if;

    if ((select count(*) from "Semester" where id = semesterId) = 0) then raise exception 'Cannot find semester. '; end if;

    RETURN QUERY
        select "sectionId", "sectionName", "leftCapacity", "totalCapacity"
        from "CourseSection"
        where "courseId" = courseId
          and "semesterId" = semesterId;
END
$$ LANGUAGE 'plpgsql';
```

#### get_Course_By_Section

比起之前的筛选、搜索方法，这个方法逆其道而行之倒是挺让我好奇的。

传入参数：sectionId integer, 表示询问的 section ID. 

传出参数：对应的 Course 的相关信息表格——这个表格有且仅有一行。

当找不到对应的 Course ID 时，丢出错误：Cannot find section. 

```sql
CREATE OR REPLACE function get_Course_By_Section(sectionId int)
    RETURNS TABLE
            (
                id         varchar,
                name       varchar,
                creditOut  integer,
                classHour  integer,
                gradingOut varchar
            )
AS
$$
BEGIN

    if ((select count(*) from "CourseSection" where "sectionId" = sectionId) = 0) then raise exception 'Cannot find section. '; end if;

    RETURN QUERY
        select "Course"."courseId", "courseName", credit, "classHour", grading
        from "CourseSection"
                 left join "Course" on "CourseSection"."courseId" = "Course"."courseId"
        where "sectionId" = sectionId;
END
$$ LANGUAGE 'plpgsql';
```

#### get_Course_Section_Classes

传入参数：sectionIdIn integer, 课段 ID 值，表示筛选出的 class 结果都应该归属于该课段。

如果该 sectioinIdIn 非法、不存在，将会得到错误：Cannot find section. 

> 我希望他在写 sql 语句的时候动点脑子。
>
> -- Cutie Deng

输出参数：表示 class 的表格，它们都归属于同一个 section, 即我们询问的 section. 

追加一个标记，可以被优化的：@Optimized

```sql
CREATE OR REPLACE function get_Course_Section_Classes(sectionIdIn int)
    RETURNS TABLE
            (
                idOut               int,
                dayOfWeek           varchar,
                weekList            smallint[],
                classBegin          smallint,
                classEnd            smallint,
                locationOut         varchar,
                instructor_fullName varchar,
                instructor_idOut    integer
            )
AS
$$
BEGIN
    if ((select count(*) from "CourseSection" where "sectionId" = sectionIdIn) = 0) then raise exception 'Cannot find section. '; end if;

    RETURN QUERY
        select "CourseSectionClass".id,
               "dayOfWeek",
               "weekList",
               "classStart",
               "classEnd",
               location,
               "fullName",
               instructor
        from "CourseSectionClass"
                 left join "Instructor" on "CourseSectionClass".instructor = "Instructor"."userId"
                 left join "User" on "Instructor"."userId" = "User".id
        where "CourseSectionClass"."sectionId" = sectionIdIn;
END
$$ LANGUAGE 'plpgsql';
```

#### get_Course_Section_By_Class

有点像上一个方法的逆运算——不过这按理来说是一个至多返回一行的函数。

我有点好奇作者在设计它的时候的小九九。

@Optimized

语义简单，不再赘述。

```sql
CREATE OR REPLACE function get_Course_Section_By_Class(classId int)
    RETURNS TABLE
            (
                sectionId     integer,
                nameOut       varchar,
                leftCapacity  integer,
                totalCapacity integer
            )
AS
$$
BEGIN

    if ((select count(*) from "CourseSectionClass" where id = classId) = 0) then raise exception 'Cannot find class. '; end if;

    RETURN QUERY
        select "CourseSection"."sectionId", "sectionName", "leftCapacity", "totalCapacity"
        from "CourseSectionClass"
                 left join "CourseSection" on "CourseSectionClass"."sectionId" = "CourseSection"."sectionId"
        where id = classId;
END
$$ LANGUAGE 'plpgsql';

```

#### remove_course_trigger_function

这个方法用于实现一个 remove course 触发器——当有同学想要 drop course 的时候，便自动将对应课程的 leftCapacity+1, 其实我并不觉得这在业务上是合理的，所以——

@Deleted

```sql
CREATE OR REPLACE FUNCTION remove_course_trigger_function()
    returns trigger
as
$$
declare
begin
    update "CourseSection" set "leftCapacity" = "leftCapacity" + 1 where "sectionId" = old."sectionId";
    return new;
end

$$ language plpgsql;

CREATE TRIGGER remove_course_trigger
    AFTER delete
    on student_section
    for each row
execute procedure remove_course_trigger_function();
```

### System Management

#### get_Enrolled_Students_In_Semester

这是一个错误的方法实现。

我都不知道这厮在干啥。

> 自娱自乐
>
> -- Cutie Deng

```sql
CREATE OR REPLACE function get_Enrolled_Students_In_Semester(courseId varchar, semesterId int)
    returns TABLE
            (
                studentId         integer,
                fullNameOut       varchar,
                enrolledDateOut   date,
                majorIdOut        int,
                majorNameOut      varchar,
                departmentIdOut   integer,
                departmentNameOut varchar
            )
AS
$$
BEGIN
    if ((select count(*) from "Course" where "courseId" = courseId) = 0) then raise exception ''; end if;

    if ((select count(*) from "Semester" where id = semesterId) = 0) then raise exception ''; end if;

    return query
        select "userId",
               "fullName",
               "enrolledDate",
               "majorId",
               "Major".name,
               "Department"."departmentId",
               "Department".name
        from "Student"
                 left join "User" on "Student"."userId" = "User".id
                 left join "Major" on "Student"."majorId" = "Major".id
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId";
END
$$ LANGUAGE 'plpgsql';
```

#### passed_Prerequisites_For_Course

先修课依赖描述：好复杂，我们跳过吧。

我觉得如果说这个 project, 有什么地方比较，稍稍难懂，那就是这部分内容了。

不过让我们摆脱这种苦恼吧，一句话把它的用处点清楚：

传入参数：

- studentIdIn integer, 描述学生 ID. 
- courseIdIn varchar, 描述所选择的课程 ID.
- pathIn ltree, 传入 NULL 即可。
- levelIn integer, 传入 NULL 即可。

返回值：

- Bool
  True: 表示该学生选择该 course 的先修课条件满足，otherwise, 不满足。

```sql
create table public.prerequisite
(
    "courseId" varchar
        constraint prerequisite_course_courseid_fk
            references public."Course"
            on delete cascade,
    path       ltree,
    level      integer,
    "No"       integer
);


CREATE OR REPLACE function passed_Prerequisites_For_Course(studentIdIn integer, courseIdIn varchar, pathIn ltree,
                                                           levelIn integer)
    RETURNS bool
AS
$$
declare
    root     varchar;
    andFlag  bool;
    orFlag   bool;
    cnt      int;
    totalCnt int;
    subPath  ltree;
BEGIN
    if (pathIn is null) then
        if ((select count(*) from prerequisite where text2ltree('Top.'||courseIdIn) @> path) = 0) then return true; end if;
        root = 'Top.' || courseIdIn;
        subpath = (select path from prerequisite where text2ltree(root) @> path and level = 3);
        return passed_Prerequisites_For_Course(studentIdIn, courseIdIn, subPath, 3);
    end if;

    root = (select split_part(ltree2text(pathIn), '.', levelIn));
    --done test
    if (root not like 'and%' and root not like 'or%') then --todo
        if (
                root in (select "courseId"
                         from student_section
                                  left join "CourseSection" CS on CS."sectionId" = student_section."sectionId"
                         where "studentId" = studentIdIn
                           and case
                                   when (grade ~ '^([0-9]+[.]?[0-9]*|[.][0-9]+)$') then
                                       cast(grade as int) between 60 and 100
                                   else grade = 'PASS'
                             end)
            ) then
            return true;
        else
            return false;
        end if;
    end if;
    totalCnt = (select count(*) from prerequisite where pathIn @> path and level = levelIn + 1); --done test
    cnt = 1;

    if (root like 'and%') then
        while cnt <= totalCnt
            loop
                subPath =
                        (select path from prerequisite where pathIn @> path and level = levelIn + 1 and "No" = cnt);
                if ((passed_Prerequisites_For_Course(studentIdIn, courseIdIn, subPath, levelIn + 1)) = false)
                then
                    return false;
                end if;
                cnt = cnt + 1;
            end loop;
        return true;
    end if;


    if (root like 'or%') then
        while cnt <= totalCnt
            loop
                subPath =
                        (select path from prerequisite where pathIn @> path and level = levelIn + 1 and "No" = cnt);
                if ((passed_Prerequisites_For_Course(studentIdIn, courseIdIn, subPath, levelIn + 1)))
                then
                    return true;
                end if;
                cnt = cnt + 1;
            end loop;
        return false;
    end if;

    return false; --todo
END
$$ LANGUAGE 'plpgsql';
```

## 笔者补充方法

### Course Service Implementation

#### add_course_section

该方法传入四个参数：

- courseIdIn varchar: 表示我们将要加入的 section 隶属于某个 course, 可能是 NULL. 笔者会在 Java 中对此进行约束，避免该错误发生。
- semesterIdIn integer: 表示新的 section 的课程是对应学期的课程。
- sectionNameIn varchar: 新 section 全名，该属性如果被传入 NULL 不会发生异常，Java 对此承诺不会出现这种现象。
- totalCapacityIn integer: 课段容量。

该方法将会返回一个 integer, 表示新课段的唯一 ID. 

除此之外，该方法还默认是实现了对 (courseId, semesterId, sectionName) 元组的 UNIQUE 约束——一旦传入一个新的相同元组，也就意味着我们最后的返回结果超过一行——这会导致 raise exception 间接使得 INSERT 失败。

而笔者再三考虑—— sectionName 应该某种意义上来说，是一个比较特别的属性——恰恰相反，totalCapacity 虽然也有一定的标识作用，但笔者还是觉得意义不大，谁没事会根据 capacity 来判断、区分课程呢？

因此，笔者最后还是断定这个 unique tuple 不应当包含 capacity 信息，以上。

```SQL
CREATE OR REPLACE FUNCTION add_course_section(courseIdIn varchar,
semesterIdIn integer, sectionNameIn varchar, totalCapacityIn integer)
    RETURNS INTEGER
    language plpgsql
as $$
    begin
        INSERT INTO "CourseSection"("courseId", "semesterId", "sectionName", "totalCapacity", "leftCapacity")
        VALUES (courseIdIn, semesterIdIn, sectionNameIn, totalCapacityIn, totalCapacityIn);
        RETURN (SELECT "CourseSection"."sectionId"
        FROM "CourseSection"
        WHERE "courseId" = courseIdIn
            AND "semesterId" = semesterIdIn
            AND "sectionName" = sectionNameIn);
    end
    $$
```

#### add_course_section_class

该方法用于为 section 添加具体的 class, 以便于让学生能够进行具体的课程学习。

该方法有 7 个传入参数：

- sectionIdIn integer, 表示该 class 对应的 section.
- instructorIdIn integer, 表示该课时授课者的 ID. Java 对它进行了一定的约束，该参数不可能是 NULL. 这无形中避免了我们之前在讨论该表格约束不足的缺陷。
- DayOfWeekIn varchar, 它对应的 Java 类型是枚举类，这意味着它只可能是七个特别字符串中的一个，同时 Java 承诺它不会被赋予一个 NULL 值。
- weekListIn smallint[], 表示该课时的周时上课要求，Java 在执行该方法时，有两点保证：weekList 不会是 NULL, 同时 weekList 不会是没有长度的数组（一个长度为 0 的数组）。
- classStartIn smallint, 表示该课时当天的开始时间。
- classEndIn smallint, 表示该课时当天的结束时间。这两个属性在 Java UI 中会被限制： $0 \leq classStart \leq classEnd$
- locationIn varchar, java constraint: NOT NULL. 

该方法将会返回一个值，描述被新加入的 class 所得到的唯一 ID. 

而笔者突然考虑到——一个具体的课时，无论如何都要承载对应的上课时间地点，我们不愿意发生这样一件事情——两个 class 被同时设置在同一时间、同一地点进行授课，那恐怕会发生一点令人捉摸不透的冲突。

:warning: 尽管如此，这件事情依旧是很可能发生的，比方说一门 course 下分列了几门 sections, 但它们的 theory class 是一门大课，这意味着各个 section 共享同一个 class 的执行情况，而我们的数据结构设置恐怕难以满足这件事情——所以这个特别的 UNIQUE 检查我还是悄悄跳过吧。

```sql
CREATE OR REPLACE FUNCTION add_course_section_class(
sectionIdIn integer,
instructorIdIn integer,
DayOfWeekIn varchar,
weekListIn smallint[],
classStartIn smallint,
classEndIn smallint,
locationIn varchar
) RETURNS INTEGER
LANGUAGE plpgsql
AS
    $$
    BEGIN
       INSERT INTO "CourseSectionClass"("sectionId", "instructor", "dayOfWeek", "weekList", "classStart", "classEnd", location)
       VALUES (sectionIdIn, instructorIdIn, DayOfWeekIn, weekListIn, classStartIn, classEndIn, locationIn);
       RETURN (
           SELECT id
           FROM "CourseSectionClass"
           WHERE "sectionId" = sectionIdIn
            AND instructor = instructorIdIn
            AND "dayOfWeek" = DayOfWeekIn
            AND "weekList" = weekListIn
            AND "classStart" = classStartIn
            AND "classEnd" = classEndIn
            AND "location" = locationIn
           );
    END
    $$
```

### Student Service Implementation

#### add_student

该方法将会为数据库中添加一个学生实体，它同时会自动、同步地向 User 表格中添加学生的相关信息，以保持表格的约束条件。

传入参数：

- userIdIn integer, 学生 ID.
- majorIdIn integer, 学生专业，虽然笔者认为这里可以设为 NULL, 但遗憾的是，通过 DBMS 去调用的结果是——该值不能为 NULL. 
- firstNameIn varchar, 学生的姓氏。
- lastNameIn varchar, 学生的名称。Java 会对姓名进行检查——虽然原则上允许等于空字符串，不过肯定不允许 NULL 的出现就是了。
- fullNameIn varchar, 学生全称，Java 会自动根据 first name, last name 相关情况进行自动合成。
- enrolledDateIn date, 学生的注册日期——没什么意义，但不允许非空的一个量。

传出：VOID

```sql
CREATE OR REPLACE FUNCTION add_student(userIdIn integer,
majorIdIn integer,
firstNameIn varchar,
lastNameIn varchar,
fullNameIn varchar,
enrolledDateIn date)
RETURNS VOID
LANGUAGE plpgsql
AS $$
    BEGIN
        INSERT INTO "User"(id, "fullName") VALUES (userIdIn, fullNameIn);
        INSERT INTO "Student"("userId", "firstName", "lastName", "enrolledDate", "majorId")
        VALUES (userIdIn, firstNameIn, lastNameIn, enrolledDateIn, majorIdIn);
    END
    $$
```

观察代码，显然我们很容易确定，add_student 方法发生错误只有一种情况：依赖关系发生错误，references 外键约束失败。

#### enroll_course

有些方法写起来难度极大，我们不妨先跳过，先完成简单的方法编写。

该方法用于提供学生选择课段的权力，所以传入参数非常简单：

- studentIdIn integer, 要选择课程的学生 ID.
- courseSectionIdIn integer, 要选择的课段 ID.

二者都不可能是 NULL 值。

返回的情况繁多，在此笔者进行详细解释：

- COURSE_NOT_FOUND: 当学生选择的 course section ID 无法被找到的情况下，则会诱发该错误。

- ALREADY_ENROLLED: 学生企图重复选择相同的课段，将会导致该错误。值得注意的是，无论这门课段该学生有无获得成绩，都不能被重复选择——重修课程应当选择下一学期对应 course 的 section. 

- ALREADY_PASSED: 
  首先，我们尝试描述一个 view 来观察各个学生完成的课程情况

  ```sql
  create or replace function enroll_course(studentidin integer, coursesectionidin integer) returns void
      language plpgsql
  as
  $$
  BEGIN
      -- 如果找不到对应的 section ID, 返回错误 COURSE_NOT_FOUND.
          IF ((SELECT count(*) FROM "CourseSection"
          WHERE "sectionId" = courseSectionIdIn) = 0) THEN
              RAISE EXCEPTION 'COURSE_NOT_FOUND';
              END IF;
  
      -- 课段已经被该学生选择了。
          IF (SELECT count(*) FROM "student_section"
          WHERE "sectionId" = courseSectionIdIn
          AND "studentId" = studentIdIn) > 0 THEN
              RAISE EXCEPTION 'ALREADY_ENROLLED';
          end if;
  
      -- 课段对应课程已经通过。
      IF (SELECT count(*)
      FROM
           (SELECT "courseId"
          FROM studentPass
          WHERE "studentId" = studentIdIn) AS proper_course
      WHERE (proper_course."courseId" = get_a_course(courseSectionIdIn))
          ) > 0 THEN
          RAISE EXCEPTION 'ALREADY_PASSED';
      end if;
  
      -- 课程前置条件不满足。
          IF NOT passed_prerequisites_for_course(studentIdIn, get_a_course(courseSectionIdIn)
              , NULL, NULL) THEN
              RAISE EXCEPTION 'PREREQUISITES_NOT_FULFILLED';
          end if;
  
      -- 选择了重复的课程。
          IF (WITH have_chosen_course AS
              (SELECT "CourseSection"."courseId"
              FROM
                  (SELECT "sectionId"
                  FROM "student_section"
                  WHERE "studentId" = studentIdIn
                      AND (grade IS NULL OR grade = 'X')) AS special_sections
              INNER JOIN "CourseSection"
                  ON "CourseSection"."sectionId" = special_sections."sectionId")
          SELECT count(*)
          FROM have_chosen_course
          WHERE "courseId" = (get_a_course(courseSectionIdIn) :: varchar)) > 0 THEN
              RAISE EXCEPTION 'COURSE_CONFLICT_FOUND';
          end if;
  
      -- 选择了时间冲突的课段
          IF
              (SELECT count(*)
                  FROM
              (SELECT "dayOfWeek", "weekList", "classStart", "classEnd"
              FROM "CourseSectionClass"
              WHERE "sectionId" IN
                  (SELECT "sectionId"
                  FROM student_section
                  WHERE "studentId" = studentIdIn AND (grade IS NULL OR grade = 'X')) ) AS compared_section
              JOIN (
                  SELECT "dayOfWeek", "weekList", "classStart", "classEnd"
                  FROM "CourseSectionClass"
                  WHERE "sectionId" = courseSectionIdIn
              ) as chosen_section ON (
                  (compared_section."weekList" && chosen_section."weekList")
                  AND (compared_section."dayOfWeek" = chosen_section."dayOfWeek")
                  AND (
                      NOT (
                          ((compared_section."classEnd" - chosen_section."classStart") *
                          (compared_section."classEnd" - chosen_section."classEnd") > 0)
                          AND
                          (
                              (chosen_section."classEnd" - compared_section."classStart") *
                              (chosen_section."classEnd" - compared_section."classEnd") > 0
                              )
                          )
                      )
                  )) > 0 THEN
              RAISE EXCEPTION 'COURSE_CONFLICT_FOUND';
          end if;
  
      -- 课段容量不足错误
          IF ((SELECT "leftCapacity"
          FROM "CourseSection"
          WHERE "sectionId" = courseSectionIdIn) <= 0) THEN
              RAISE EXCEPTION 'COURSE_IS_FULL';
          end if;
  
          INSERT INTO student_section("studentId", "sectionId") VALUES (studentIdIn, courseSectionIdIn);
  
      END
  $$;
  
  ```

  ~~而后我们便能够通过 `get_Course_By_Section` 和该 view 便能够推断出这门课程是否被通过了！~~

  值得注意，该 view 会自动过滤掉 NULL 值的 courseId, 所以我们可以在之后使用该 view 的时候可以大可不必那么草木皆兵。

- PREREQUISITES_NOT_FULFILLED: 调用原 project 的方法来判断对应的 prerequisites 是否满足。

- COURSE_CONFLICT_FOUND: 课时安排冲突，或选择了重复的 course. 

- COURSE_IS_FULL: 该课段剩余容量为零。

该 function 有点冗长——以至于作者本人也吃不准这到底对不对。

```sql
CREATE OR REPLACE FUNCTION enroll_course(
studentIdIn integer,
courseSectionIdIn integer)
RETURNS VOID
LANGUAGE plpgsql
AS
$$
BEGIN
    -- 如果找不到对应的 section ID, 返回错误 COURSE_NOT_FOUND.
        IF ((SELECT count(*) FROM "student_section"
        WHERE "sectionId" = courseSectionIdIn) = 0) THEN
            RAISE EXCEPTION 'COURSE_NOT_FOUND';
            END IF;

    -- 课段已经被该学生选择了。
        IF (SELECT count(*) FROM "student_section"
        WHERE "sectionId" = courseSectionIdIn
        AND "studentId" = studentIdIn) > 0 THEN
            RAISE EXCEPTION 'ALREADY_ENROLLED';
        end if;

    -- 课段对应课程已经通过。
    IF (SELECT count(*)
    FROM
         (SELECT "courseId"
        FROM studentPass
        WHERE "studentId" = studentIdIn) AS proper_course
    WHERE (proper_course."courseId" = get_a_course(courseSectionIdIn))
        ) > 0 THEN
        RAISE EXCEPTION 'ALREADY_PASSED';
    end if;

    -- 课程前置条件不满足。
        IF NOT passed_prerequisites_for_course(studentIdIn, get_a_course(courseSectionIdIn)
            , NULL, NULL) THEN
            RAISE EXCEPTION 'PREREQUISITES_NOT_FULFILLED';
        end if;

    -- 选择了重复的课程。
        IF (WITH have_chosen_course AS
            (SELECT "CourseSection"."courseId"
            FROM
                (SELECT "sectionId"
                FROM "student_section"
                WHERE "studentId" = studentIdIn
                    AND (grade IS NULL OR grade = 'X')) AS special_sections
            INNER JOIN "CourseSection"
                ON "CourseSection"."sectionId" = special_sections."sectionId")
        SELECT count(*)
        FROM have_chosen_course
        WHERE "courseId" = (get_a_course(courseSectionIdIn) :: varchar)) > 0 THEN
            RAISE EXCEPTION 'COURSE_CONFLICT_FOUND';
        end if;

    -- 选择了时间冲突的课段
        IF
            (SELECT count(*)
                FROM
            (SELECT "dayOfWeek", "weekList", "classStart", "classEnd"
            FROM "CourseSectionClass"
            WHERE "sectionId" IN
                (SELECT "sectionId"
                FROM student_section
                WHERE "studentId" = studentIdIn AND (grade IS NULL OR grade = 'X')) ) AS compared_section
            JOIN (
                SELECT "dayOfWeek", "weekList", "classStart", "classEnd"
                FROM "CourseSectionClass"
                WHERE "sectionId" = courseSectionIdIn
            ) as chosen_section ON (
                (compared_section."weekList" && chosen_section."weekList")
                AND (compared_section."dayOfWeek" = chosen_section."dayOfWeek")
                AND (
                    NOT (
                        ((compared_section."classEnd" - chosen_section."classStart") *
                        (compared_section."classEnd" - chosen_section."classEnd") > 0)
                        AND
                        (
                            (chosen_section."classEnd" - compared_section."classStart") *
                            (chosen_section."classEnd" - compared_section."classEnd") > 0
                            )
                        )
                    )
                )) > 0 THEN
            RAISE EXCEPTION 'COURSE_CONFLICT_FOUND';
        end if;

    -- 课段容量不足错误
        IF ((SELECT "leftCapacity"
        FROM "CourseSection"
        WHERE "sectionId" = courseSectionIdIn) <= 0) THEN
            RAISE EXCEPTION 'COURSE_IS_FULL';
        end if;

        INSERT INTO student_section("studentId", "sectionId") VALUES (studentIdIn, courseSectionIdIn);

    END
$$;

```

看着办吧，全凭运气。

#### drop_selection

传入参数：

- studentIdIn integer, drop selection 主体用户（学生）的 ID.
- sectionIdIn integer, 学生推掉的课段 ID.

没有传出参数。

```sql
create or replace function drop_selection(studentidin integer, sectionidin integer) returns void
    language plpgsql
as
$$
BEGIN
        IF ((SELECT count(*) FROM student_section
            WHERE "sectionId" = sectionIdIn
            AND "studentId" = studentIdIn) = 0) THEN RAISE EXCEPTION 'Cannot find section. ' ; END IF;
        IF ((SELECT count(*) FROM student_section
            WHERE "sectionId" = sectionIdIn
            AND "studentId" = studentIdIn
                AND grade IS NOT NULL
                AND grade <> 'X') <> 0) THEN RAISE EXCEPTION 'Finished the section.' ; END IF;

        IF ((SELECT count(*) FROM student_section
            WHERE "sectionId" = sectionIdIn
            AND "studentId" = studentIdIn
                AND grade IS NULL) > 0) THEN
            UPDATE "CourseSection" SET "leftCapacity" = "leftCapacity" + 1
            WHERE "sectionId" = sectionIdIn;
        end if;

        DELETE FROM student_section WHERE "sectionId" = sectionIdIn
        AND "studentId" = studentIdIn;
    end;
$$;
```

#### get_a_course

简单课程查询——原有方法过于拉垮。

```sql
CREATE FUNCTION get_a_course(sectionIdIn integer)
RETURNS varchar
LANGUAGE plpgsql
AS $$
    BEGIN
        RETURN (
            SELECT "courseId"
            FROM "CourseSection"
            WHERE "sectionId" = sectionIdIn
            );
    end;
    $$
```

#### kill_big_bang

删库跑路。

```sql
create function kill_big_bang() returns void
    language plpgsql
as
$$
BEGIN
        TRUNCATE "Course" CASCADE ;
        TRUNCATE "CourseSection" CASCADE ;
        TRUNCATE "CourseSectionClass" CASCADE ;
        TRUNCATE "Department" CASCADE ;
        TRUNCATE "Instructor" CASCADE ;
        TRUNCATE "Major" CASCADE ;
        TRUNCATE "Major_Course" CASCADE ;
        TRUNCATE "Semester" CASCADE ;
        TRUNCATE "Student" CASCADE ;
        TRUNCATE student_section CASCADE ;
        TRUNCATE "User" CASCADE ;
        TRUNCATE "prerequisite" CASCADE ;

        ALTER SEQUENCE "CourseSection_sectionId_seq" restart with 1;
        ALTER SEQUENCE "CourseSectionClass_id_seq" restart with 1;
        ALTER SEQUENCE "Department_departmentId_seq" restart with 1;
        ALTER SEQUENCE "Major_Course_id_seq" restart with 1;
        ALTER SEQUENCE "Major_id_seq" restart with 1;
        ALTER SEQUENCE "Semester_id_seq" restart with 1;
    END;
$$;

alter function kill_big_bang() owner to postgres;
```

### 补充

#### drop_selection

之所以要补充该方法描述——这是因为笔者拿不准 project “希望”我们做什么。

```sql
create or replace function drop_selection(studentidin integer, sectionidin integer) returns void
    language plpgsql
as
$$
BEGIN
        IF ((SELECT count(*) FROM student_section
            WHERE "sectionId" = sectionIdIn
            AND "studentId" = studentIdIn) = 0) THEN RAISE EXCEPTION 'Cannot find section. ' ; END IF;
        IF ((SELECT count(*) FROM student_section
            WHERE "sectionId" = sectionIdIn
            AND "studentId" = studentIdIn
                AND grade IS NOT NULL
                AND grade <> 'X') <> 0) THEN RAISE EXCEPTION 'Finished the section.' ; END IF;
        IF ((SELECT count(*) FROM student_section
            WHERE "sectionId" = sectionIdIn
            AND "studentId" = studentIdIn
                AND grade IS NULL) > 0 or true) THEN
            UPDATE "CourseSection" SET "leftCapacity" = "leftCapacity" + 1
            WHERE "sectionId" = sectionIdIn;
        end if;

        DELETE FROM student_section WHERE "sectionId" = sectionIdIn
        AND "studentId" = studentIdIn;
    end;
$$;

alter function drop_selection(integer, integer) owner to postgres;
```
