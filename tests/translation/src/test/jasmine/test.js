describe("A suite", function() {
  it("contains spec with an expectation", function() {
    var test = new org.treblereel.j2cl.exports.ExportTestClass();

    expect(test.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    expect(test.id).toBe('qwerty');

    expect(org.treblereel.j2cl.exports.ExportTestClass.test2('STATIC METHOD CALL')).toBe('STATIC METHOD CALL');
    expect(org.treblereel.j2cl.exports.ExportTestClass.staticProperty).toBe('staticProperty');

    var test2 = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass();

    expect(test2.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    expect(test2.id).toBe('qwerty');

    expect(org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass.test2('STATIC METHOD CALL')).toBe('STATIC METHOD CALL');
    expect(org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass.staticProperty).toBe('staticProperty');

  });
});