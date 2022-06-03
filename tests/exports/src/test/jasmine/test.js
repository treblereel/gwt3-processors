describe("@GWT3Export test suite", function() {

  it('check promise returned', function(done) {
    new org.treblereel.j2cl.exports.ExportTestClass().promise1('PROMISE CALL EXPECTED').then(function(result) {
      expect(result).toBe('PROMISE CALL EXPECTED');
      done();
    });
  });

  it('Should be 2-arg promise', function(done) {
    new org.treblereel.j2cl.exports.ExportTestClass().promise2('PROMISE CALL EXPECTED', 222).then(function(result) {
      expect(result).toBe('PROMISE CALL EXPECTED+222');
      done();
    });
  });

  it("test ExportTestClass", function() {

    var test = new org.treblereel.j2cl.exports.ExportTestClass();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(org.treblereel.j2cl.exports.ExportTestClass.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new org.treblereel.j2cl.exports.ExportTestClass();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');

    expect(test.new_custome_method_name()).toBe('new_custome_method_name');

  });

  it("test A_B_C_D_ExportTestClass", function() {

    var test = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');
  });

  it("test JsType", function() {
    var test = new org.treblereel.j2cl.exports.JsMethodsTester();
    expect(test.test1('JsMethodsTester')).toBe('JsMethodsTester');
    expect(org.treblereel.j2cl.exports.JsMethodsTester.test2('JsMethodsTester')).toBe('JsMethodsTester');
    expect(test.test3()).toBe('staticProperty');
    expect(org.treblereel.j2cl.exports.JsMethodsTester.test4()).toBe('staticProperty');
    expect(test.test5()).toBe('qwerty');
    expect(test.new_custome_method_name()).toBe('new_custome_method_name');
  });

  it("test ClassWithGWT3ExportOnDecl", function() {
    var test = new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');

    expect(test.new_custome_method_name()).toBe('new_custome_method_name');

  });

  it("test ClassWithGWT3ExportCustomName", function() {
    var test = new QWERTY_OLOLO();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(QWERTY_OLOLO.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new QWERTY_OLOLO();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');

  });

  it("test ClassWithGWT3ExportCustomNameAndDefaultPkg", function() {
    var test = new org.treblereel.j2cl.exports.QWERTY_OLOLO();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(org.treblereel.j2cl.exports.QWERTY_OLOLO.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new org.treblereel.j2cl.exports.QWERTY_OLOLO();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');

  });

  it("test ClassWithGWT3ExportCustomPkg", function() {
    var test = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');

  });

  it("test ClassWithGWT3ExportCustomName2", function() {
    var test = new org.treblereel.j2cl.processors.TestBean();

    expect(test.test()).toBe('ExportTestClass');
    expect(test.test1('ExportTestClass')).toBe('ExportTestClass');

    var arr = ['a','b','c','d'];
    expect(test.test2(arr)).toBe(arr);
    expect(test.test3(arr)).toBe(arr);

    expect(test.test4('ExportTestClass',444)).toBe('ExportTestClass+444');

    expect(org.treblereel.j2cl.processors.TestBean.staticTest('ExportTestClass')).toBe('ExportTestClass');


    expect(test.getValue()).toBe('default value');

    test.setValue('new text value');
    expect(test.getValue()).toBe('new text value');

    var test2 = new org.treblereel.j2cl.processors.TestBean();

    expect(test2.getValue()).toBe('default value');
    expect(test.getValue()).toBe('new text value');

    test2.setValue('new text value 2');
    expect(test.getValue()).toBe('new text value');
    expect(test2.getValue()).toBe('new text value 2');

  });

  it("Interface usage 1", function() {
    var test = new org.treblereel.j2cl.exports.impl.v1_0.MyInterface();

    expect(test.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
  });

  it("Interface usage 2", function() {
    var test = new org.treblereel.j2cl.exports.impl.v10.MyInterface();

    expect(test.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
  });

});