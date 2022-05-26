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
});